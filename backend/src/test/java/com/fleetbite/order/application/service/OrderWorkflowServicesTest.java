package com.fleetbite.order.application.service;

import java.util.UUID;

import com.fleetbite.order.application.dto.CancelOrderCommand;
import com.fleetbite.order.application.port.out.DomainEventPublisherPort;
import com.fleetbite.order.application.port.out.OrderHistoryRepositoryPort;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.event.OrderReadyEvent;
import com.fleetbite.order.domain.exception.InvalidOrderDataException;
import com.fleetbite.order.domain.exception.InvalidOrderTransitionException;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderHistoryEvent;
import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderWorkflowServicesTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 20, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime NOW =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Clock FIXED_CLOCK = Clock.fixed(NOW.toInstant(), BusinessTime.ZONE_OFFSET);

	@Mock
	private OrderRepositoryPort orderRepositoryPort;
	@Mock
	private OrderHistoryRepositoryPort orderHistoryRepositoryPort;
	@Mock
	private DomainEventPublisherPort domainEventPublisherPort;

	private ConfirmOrderService confirmOrderService;
	private StartOrderPreparationService startOrderPreparationService;
	private MarkOrderReadyService markOrderReadyService;
	private CancelOrderService cancelOrderService;

	@BeforeEach
	void setUp() {
		OrderHistoryRecorder recorder = new OrderHistoryRecorder(orderHistoryRepositoryPort);
		confirmOrderService = new ConfirmOrderService(orderRepositoryPort, recorder, FIXED_CLOCK);
		startOrderPreparationService = new StartOrderPreparationService(orderRepositoryPort, recorder, FIXED_CLOCK);
		markOrderReadyService = new MarkOrderReadyService(
				orderRepositoryPort,
				recorder,
				domainEventPublisherPort,
				FIXED_CLOCK);
		cancelOrderService = new CancelOrderService(orderRepositoryPort, recorder, FIXED_CLOCK);
	}

	@Test
	void confirm_shouldTransitionAndRecordHistory() {
		Order order = createdOrder();
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(orderRepositoryPort.update(any())).thenAnswer(i -> i.getArgument(0));

		var result = confirmOrderService.execute(order.id());

		assertEquals(OrderStatus.CONFIRMED, result.status());
		assertHistory(OrderHistoryEventType.ORDER_CONFIRMED, OrderStatus.CREATED, OrderStatus.CONFIRMED, null);
	}

	@Test
	void confirm_shouldRejectInvalidTransition() {
		Order order = createdOrder();
		order.confirm(CREATED.plusMinutes(1));
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));

		assertThrows(InvalidOrderTransitionException.class, () -> confirmOrderService.execute(order.id()));
		verify(orderHistoryRepositoryPort, never()).save(any());
	}

	@Test
	void startPreparation_shouldTransitionAndRecordHistory() {
		Order order = createdOrder();
		order.confirm(CREATED.plusMinutes(1));
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(orderRepositoryPort.update(any())).thenAnswer(i -> i.getArgument(0));

		var result = startOrderPreparationService.execute(order.id());

		assertEquals(OrderStatus.PREPARING, result.status());
		assertHistory(OrderHistoryEventType.ORDER_PREPARING, OrderStatus.CONFIRMED, OrderStatus.PREPARING, null);
	}

	@Test
	void ready_shouldTransitionRecordHistoryAndPublishOrderReadyEvent() {
		Order order = createdOrder();
		order.confirm(CREATED.plusMinutes(1));
		order.startPreparation(CREATED.plusMinutes(2));
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(orderRepositoryPort.update(any())).thenAnswer(i -> i.getArgument(0));

		var result = markOrderReadyService.execute(order.id());

		assertEquals(OrderStatus.READY, result.status());
		assertHistory(OrderHistoryEventType.ORDER_READY, OrderStatus.PREPARING, OrderStatus.READY, null);

		ArgumentCaptor<OrderReadyEvent> eventCaptor = ArgumentCaptor.forClass(OrderReadyEvent.class);
		verify(domainEventPublisherPort, times(1)).publish(eventCaptor.capture());
		OrderReadyEvent event = eventCaptor.getValue();
		assertEquals(order.id(), event.orderId());
		assertEquals(NOW, event.occurredAt());
		assertEquals(BusinessTime.ZONE_OFFSET, event.occurredAt().getOffset());
	}

	@Test
	void ready_shouldNotPublishWhenPersistFails() {
		Order order = createdOrder();
		order.confirm(CREATED.plusMinutes(1));
		order.startPreparation(CREATED.plusMinutes(2));
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(orderRepositoryPort.update(any())).thenThrow(new RuntimeException("persist failed"));

		assertThrows(RuntimeException.class, () -> markOrderReadyService.execute(order.id()));
		verify(domainEventPublisherPort, never()).publish(any());
		verify(orderHistoryRepositoryPort, never()).save(any());
	}

	@Test
	void cancel_shouldStoreReasonOnlyInHistory() {
		Order order = createdOrder();
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(orderRepositoryPort.update(any())).thenAnswer(i -> i.getArgument(0));

		var result = cancelOrderService.execute(order.id(), new CancelOrderCommand("Customer requested cancellation"));

		assertEquals(OrderStatus.CANCELLED, result.status());
		assertHistory(
				OrderHistoryEventType.ORDER_CANCELLED,
				OrderStatus.CREATED,
				OrderStatus.CANCELLED,
				"Customer requested cancellation");
	}

	@Test
	void cancel_shouldRejectBlankReason() {
		Order order = createdOrder();

		assertThrows(
				InvalidOrderDataException.class,
				() -> cancelOrderService.execute(order.id(), new CancelOrderCommand("   ")));
		verify(orderRepositoryPort, never()).update(any());
		verify(orderHistoryRepositoryPort, never()).save(any());
	}

	@Test
	void cancel_shouldRejectInvalidStatus() {
		Order order = createdOrder();
		order.confirm(CREATED.plusMinutes(1));
		order.startPreparation(CREATED.plusMinutes(2));
		order.markReady(CREATED.plusMinutes(3));
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));

		assertThrows(
				InvalidOrderTransitionException.class,
				() -> cancelOrderService.execute(order.id(), new CancelOrderCommand(null)));
	}

	private void assertHistory(
			OrderHistoryEventType type,
			OrderStatus previous,
			OrderStatus next,
			String description) {
		ArgumentCaptor<OrderHistoryEvent> captor = ArgumentCaptor.forClass(OrderHistoryEvent.class);
		verify(orderHistoryRepositoryPort).save(captor.capture());
		OrderHistoryEvent event = captor.getValue();
		assertEquals(type, event.eventType());
		assertEquals(previous, event.previousStatus());
		assertEquals(next, event.newStatus());
		assertEquals(description, event.description());
		assertEquals(NOW, event.createdAt());
	}

	private static Order createdOrder() {
		return Order.create(
				UUID.randomUUID(),
				OrderCode.of("ORD-2026-FLOW1"),
				"Ana",
				"999",
				"Addr",
				new Location(-12.1, -77.0),
				Money.of(new BigDecimal("10.00")),
				CREATED,
				CREATED.plusMinutes(45));
	}
}
