package com.fleetbite.order.application.service;

import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderId;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListOrdersServiceTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime PROMISED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 45, 0, 0, BusinessTime.ZONE_OFFSET);

	@Mock
	private OrderRepositoryPort orderRepositoryPort;

	private ListOrdersService listOrdersService;

	@BeforeEach
	void setUp() {
		listOrdersService = new ListOrdersService(orderRepositoryPort);
	}

	@Test
	void execute_shouldReturnMappedOrders() {
		Order order = sampleOrder("ORD-2026-AAAA1111");
		when(orderRepositoryPort.findAll()).thenReturn(List.of(order));

		List<OrderResult> results = listOrdersService.execute();

		assertEquals(1, results.size());
		assertEquals(order.id().value(), results.getFirst().id());
		verify(orderRepositoryPort).findAll();
	}

	@Test
	void execute_shouldReturnEmptyList() {
		when(orderRepositoryPort.findAll()).thenReturn(List.of());

		List<OrderResult> results = listOrdersService.execute();

		assertTrue(results.isEmpty());
	}

	private static Order sampleOrder(String code) {
		return Order.create(
				OrderId.generate(),
				OrderCode.of(code),
				"Ana Torres",
				"999999999",
				"Av. Example 123",
				new Location(-12.1001, -77.0201),
				Money.of(new BigDecimal("85.90")),
				CREATED_AT,
				PROMISED_AT);
	}
}
