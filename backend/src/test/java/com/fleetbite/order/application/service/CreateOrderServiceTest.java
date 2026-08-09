package com.fleetbite.order.application.service;

import com.fleetbite.order.application.dto.CreateOrderCommand;
import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.exception.InvalidOrderDataException;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderPriority;
import com.fleetbite.order.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

	@Mock
	private OrderRepositoryPort orderRepositoryPort;

	private CreateOrderService createOrderService;

	@BeforeEach
	void setUp() {
		createOrderService = new CreateOrderService(orderRepositoryPort);
	}

	@Test
	void execute_shouldCreateOrderPersistAndReturnResultFromSavedOrder() {
		CreateOrderCommand command = validCommand();
		when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		OrderResult result = createOrderService.execute(command);

		ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
		verify(orderRepositoryPort).save(orderCaptor.capture());
		Order saved = orderCaptor.getValue();

		assertEquals(OrderStatus.CREATED, saved.status());
		assertEquals(OrderPriority.NORMAL, saved.priority());
		assertTrue(saved.code().value().startsWith("ORD-" + LocalDate.now().getYear() + "-"));
		assertEquals(command.customerName(), result.customerName());
		assertEquals(OrderStatus.CREATED, result.status());
		assertEquals(OrderPriority.NORMAL, result.priority());
		assertNotNull(result.id());
		assertNotNull(result.createdAt());
		assertNull(result.confirmedAt());
		assertNull(result.readyAt());
		assertNull(result.deliveredAt());
	}

	@Test
	void execute_shouldPropagateInvalidOrderDataAndNotPersist() {
		CreateOrderCommand command = new CreateOrderCommand(
				"   ",
				"999999999",
				"Av. Example 123",
				-12.1001,
				-77.0201,
				new BigDecimal("85.90"),
				Instant.now().plus(45, ChronoUnit.MINUTES));

		assertThrows(InvalidOrderDataException.class, () -> createOrderService.execute(command));
		verify(orderRepositoryPort, never()).save(any());
	}

	private static CreateOrderCommand validCommand() {
		return new CreateOrderCommand(
				"Ana Torres",
				"999999999",
				"Av. Example 123",
				-12.1001,
				-77.0201,
				new BigDecimal("85.90"),
				Instant.now().plus(45, ChronoUnit.MINUTES));
	}
}
