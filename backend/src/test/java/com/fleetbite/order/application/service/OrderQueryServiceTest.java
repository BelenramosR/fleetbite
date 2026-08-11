package com.fleetbite.order.application.service;

import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.out.OrderHistoryRepositoryPort;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderHistoryEvent;
import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime PROMISED_AT = CREATED_AT.plusMinutes(45);

	@Mock
	private OrderRepositoryPort orderRepositoryPort;
	@Mock
	private OrderHistoryRepositoryPort orderHistoryRepositoryPort;

	private OrderQueryService orderQueryService;

	@BeforeEach
	void setUp() {
		orderQueryService = new OrderQueryService(orderRepositoryPort, orderHistoryRepositoryPort);
	}

	@Test
	void getByIdReturnsMappedOrder() {
		Order order = sampleOrder();
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));

		OrderResult result = orderQueryService.getById(order.id());

		assertEquals(order.id(), result.id());
		assertEquals(order.code().value(), result.code());
		assertEquals(OrderStatus.CREATED, result.status());
		verify(orderRepositoryPort).findById(order.id());
	}

	@Test
	void getByIdRejectsMissingOrder() {
		UUID orderId = UUID.randomUUID();
		when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> orderQueryService.getById(orderId));
	}

	@Test
	void findAllReturnsMappedOrders() {
		Order order = sampleOrder();
		when(orderRepositoryPort.findPage(0, 100)).thenReturn(List.of(order));

		List<OrderResult> results = orderQueryService.findPage(0, 100);

		assertEquals(1, results.size());
		assertEquals(order.id(), results.getFirst().id());
	}

	@Test
	void findAllReturnsEmptyList() {
		when(orderRepositoryPort.findPage(0, 100)).thenReturn(List.of());
		assertTrue(orderQueryService.findPage(0, 100).isEmpty());
	}

	@Test
	void getHistoryReturnsMappedEvents() {
		Order order = sampleOrder();
		OrderHistoryEvent event = OrderHistoryEvent.record(
				order.id(),
				OrderHistoryEventType.ORDER_CREATED,
				null,
				OrderStatus.CREATED,
				null,
				CREATED_AT);
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(orderHistoryRepositoryPort.findByOrderId(order.id())).thenReturn(List.of(event));

		var results = orderQueryService.getHistory(order.id());

		assertEquals(1, results.size());
		assertEquals(event.id(), results.getFirst().id());
		assertEquals(OrderHistoryEventType.ORDER_CREATED, results.getFirst().eventType());
	}

	@Test
	void getHistoryRejectsMissingOrder() {
		UUID orderId = UUID.randomUUID();
		when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> orderQueryService.getHistory(orderId));
	}

	private static Order sampleOrder() {
		return Order.create(
				UUID.randomUUID(),
				OrderCode.of("ORD-2026-AAAA1111"),
				"Ana Torres",
				"999999999",
				"Av. Example 123",
				new Location(-12.1001, -77.0201),
				Money.of(new BigDecimal("85.90")),
				CREATED_AT,
				PROMISED_AT);
	}
}
