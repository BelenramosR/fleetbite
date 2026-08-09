package com.fleetbite.order.application.service;

import com.fleetbite.order.application.dto.CreateOrderCommand;
import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.out.OrderHistoryRepositoryPort;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.exception.InvalidOrderDataException;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderHistoryEvent;
import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderPriority;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

	private static final OffsetDateTime FIXED_CREATED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime FIXED_PROMISED =
			OffsetDateTime.of(2026, 8, 8, 22, 45, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Clock FIXED_CLOCK =
			Clock.fixed(FIXED_CREATED.toInstant(), BusinessTime.ZONE_OFFSET);

	@Mock
	private OrderRepositoryPort orderRepositoryPort;
	@Mock
	private OrderHistoryRepositoryPort orderHistoryRepositoryPort;

	private CreateOrderService createOrderService;

	@BeforeEach
	void setUp() {
		createOrderService = new CreateOrderService(
				orderRepositoryPort,
				new OrderHistoryRecorder(orderHistoryRepositoryPort),
				FIXED_CLOCK);
	}

	@Test
	void execute_shouldCreateOrderPersistHistoryAndReturnResult() {
		CreateOrderCommand command = validCommand();
		when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		OrderResult result = createOrderService.execute(command);

		ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
		verify(orderRepositoryPort).save(orderCaptor.capture());
		Order saved = orderCaptor.getValue();

		assertEquals(OrderStatus.CREATED, saved.status());
		assertEquals(OrderPriority.NORMAL, saved.priority());
		assertEquals(FIXED_CREATED, saved.createdAt());
		assertEquals(FIXED_PROMISED, saved.promisedDeliveryAt());
		assertTrue(saved.code().value().startsWith(
				"ORD-" + LocalDate.ofInstant(FIXED_CLOCK.instant(), BusinessTime.ZONE_OFFSET).getYear() + "-"));
		assertEquals(command.customerName(), result.customerName());
		assertEquals(FIXED_CREATED, result.createdAt());
		assertEquals(OrderStatus.CREATED, result.status());
		assertNull(result.confirmedAt());

		ArgumentCaptor<OrderHistoryEvent> historyCaptor = ArgumentCaptor.forClass(OrderHistoryEvent.class);
		verify(orderHistoryRepositoryPort).save(historyCaptor.capture());
		OrderHistoryEvent history = historyCaptor.getValue();
		assertEquals(OrderHistoryEventType.ORDER_CREATED, history.eventType());
		assertNull(history.previousStatus());
		assertEquals(OrderStatus.CREATED, history.newStatus());
		assertEquals(FIXED_CREATED, history.createdAt());
	}

	@Test
	void execute_shouldPropagateInvalidOrderDataAndNotPersist() {
		CreateOrderCommand command = new CreateOrderCommand(
				"   ",
				"999999999",
				"Av. Example 123",
				-12.0464,
				-77.0428,
				new BigDecimal("25.50"));

		assertThrows(InvalidOrderDataException.class, () -> createOrderService.execute(command));
		verify(orderRepositoryPort, never()).save(any());
		verify(orderHistoryRepositoryPort, never()).save(any());
	}

	private static CreateOrderCommand validCommand() {
		return new CreateOrderCommand(
				"Ana Perez",
				"999999999",
				"Av. Example 123",
				-12.0464,
				-77.0428,
				new BigDecimal("25.50"));
	}
}
