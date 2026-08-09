package com.fleetbite.order.application.service;

import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.exception.OrderNotDeletableException;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderId;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteOrderServiceTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime PROMISED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 45, 0, 0, BusinessTime.ZONE_OFFSET);

	@Mock
	private OrderRepositoryPort orderRepositoryPort;

	private DeleteOrderService deleteOrderService;

	@BeforeEach
	void setUp() {
		deleteOrderService = new DeleteOrderService(orderRepositoryPort);
	}

	@Test
	void execute_shouldDeleteCreatedOrder() {
		Order order = sampleOrder();
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));

		deleteOrderService.execute(order.id());

		verify(orderRepositoryPort).deleteById(order.id());
	}

	@Test
	void execute_shouldThrowWhenMissing() {
		OrderId orderId = OrderId.generate();
		when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> deleteOrderService.execute(orderId));
		verify(orderRepositoryPort, never()).deleteById(orderId);
	}

	@Test
	void execute_shouldRejectConfirmedOrder() {
		Order order = sampleOrder();
		order.confirm(CREATED_AT.plusMinutes(1));
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));

		assertThrows(OrderNotDeletableException.class, () -> deleteOrderService.execute(order.id()));
		verify(orderRepositoryPort, never()).deleteById(order.id());
	}

	private static Order sampleOrder() {
		return Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-DEL00001"),
				"Ana Torres",
				"999999999",
				"Av. Example 123",
				new Location(-12.1001, -77.0201),
				Money.of(new BigDecimal("85.90")),
				CREATED_AT,
				PROMISED_AT);
	}
}
