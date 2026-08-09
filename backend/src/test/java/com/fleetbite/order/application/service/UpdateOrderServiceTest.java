package com.fleetbite.order.application.service;

import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.dto.UpdateOrderCommand;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.exception.OrderNotEditableException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateOrderServiceTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime PROMISED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 45, 0, 0, BusinessTime.ZONE_OFFSET);

	@Mock
	private OrderRepositoryPort orderRepositoryPort;

	private UpdateOrderService updateOrderService;

	@BeforeEach
	void setUp() {
		updateOrderService = new UpdateOrderService(orderRepositoryPort);
	}

	@Test
	void execute_shouldUpdateCreatedOrder() {
		Order order = sampleOrder();
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(orderRepositoryPort.update(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		UpdateOrderCommand command = new UpdateOrderCommand(
				"Luis Gomez",
				"988000111",
				"Calle Nueva 10",
				-12.11,
				-77.03,
				new BigDecimal("40.00"));

		OrderResult result = updateOrderService.execute(order.id(), command);

		assertEquals("Luis Gomez", result.customerName());
		assertEquals(OrderStatus.CREATED, result.status());
		assertEquals(order.code().value(), result.code());
		assertEquals(CREATED_AT, result.createdAt());
		assertEquals(PROMISED_AT, result.promisedDeliveryAt());
		verify(orderRepositoryPort).update(order);
	}

	@Test
	void execute_shouldThrowWhenOrderMissing() {
		OrderId orderId = OrderId.generate();
		when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> updateOrderService.execute(orderId, validCommand()));
		verify(orderRepositoryPort, never()).update(any());
	}

	@Test
	void execute_shouldRejectNonEditableStatus() {
		Order order = sampleOrder();
		order.confirm(CREATED_AT.plusMinutes(1));
		order.startPreparation(CREATED_AT.plusMinutes(2));
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));

		assertThrows(OrderNotEditableException.class,
				() -> updateOrderService.execute(order.id(), validCommand()));
		verify(orderRepositoryPort, never()).update(any());
	}

	private static UpdateOrderCommand validCommand() {
		return new UpdateOrderCommand(
				"Luis Gomez",
				"988000111",
				"Calle Nueva 10",
				-12.11,
				-77.03,
				new BigDecimal("40.00"));
	}

	private static Order sampleOrder() {
		return Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-UPD00001"),
				"Ana Torres",
				"999999999",
				"Av. Example 123",
				new Location(-12.1001, -77.0201),
				Money.of(new BigDecimal("85.90")),
				CREATED_AT,
				PROMISED_AT);
	}
}
