package com.fleetbite.order.application.service;

import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderId;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrderByIdServiceTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime PROMISED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 45, 0, 0, BusinessTime.ZONE_OFFSET);

	@Mock
	private OrderRepositoryPort orderRepositoryPort;

	private GetOrderByIdService getOrderByIdService;

	@BeforeEach
	void setUp() {
		getOrderByIdService = new GetOrderByIdService(orderRepositoryPort);
	}

	@Test
	void execute_shouldReturnOrderResultWhenOrderExists() {
		Order order = existingOrder();
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));

		OrderResult result = getOrderByIdService.execute(order.id());

		assertEquals(order.id().value(), result.id());
		assertEquals(order.code().value(), result.code());
		assertEquals(OrderStatus.CREATED, result.status());
		assertEquals(CREATED_AT, result.createdAt());
		assertEquals(PROMISED_AT, result.promisedDeliveryAt());
		verify(orderRepositoryPort).findById(order.id());
	}

	@Test
	void execute_shouldThrowWhenOrderDoesNotExist() {
		OrderId orderId = OrderId.generate();
		when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> getOrderByIdService.execute(orderId));

		assertEquals("RESOURCE_NOT_FOUND", exception.getCode());
	}

	private static Order existingOrder() {
		return Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-ABCDEF12"),
				"Ana Torres",
				"999999999",
				"Av. Example 123",
				new Location(-12.1001, -77.0201),
				Money.of(new BigDecimal("85.90")),
				CREATED_AT,
				PROMISED_AT);
	}
}
