package com.fleetbite.order.infrastructure.outbound.persistence;

import java.util.UUID;

import com.fleetbite.order.application.port.out.OrderHistoryRepositoryPort;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderHistoryEvent;
import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest(properties = {
		"spring.jpa.hibernate.ddl-auto=validate",
		"spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
		OrderRepositoryAdapter.class,
		OrderPersistenceMapperImpl.class,
		OrderHistoryRepositoryAdapter.class,
		OrderHistoryPersistenceMapperImpl.class
})
@Testcontainers
class OrderHistoryRepositoryAdapterIntegrationTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

	@Autowired
	private OrderRepositoryPort orderRepositoryPort;

	@Autowired
	private OrderHistoryRepositoryPort orderHistoryRepositoryPort;

	@Test
	void saveAndFindByOrderId_shouldReturnChronologicalEvents() {
		Order order = orderRepositoryPort.save(sampleOrder("ORD-2026-HIST0001"));

		orderHistoryRepositoryPort.save(OrderHistoryEvent.record(
				order.id(),
				OrderHistoryEventType.ORDER_CREATED,
				null,
				OrderStatus.CREATED,
				null,
				CREATED_AT));
		orderHistoryRepositoryPort.save(OrderHistoryEvent.record(
				order.id(),
				OrderHistoryEventType.ORDER_CONFIRMED,
				OrderStatus.CREATED,
				OrderStatus.CONFIRMED,
				null,
				CREATED_AT.plusMinutes(1)));
		orderHistoryRepositoryPort.save(OrderHistoryEvent.record(
				order.id(),
				OrderHistoryEventType.ORDER_CANCELLED,
				OrderStatus.CONFIRMED,
				OrderStatus.CANCELLED,
				"Customer requested cancellation",
				CREATED_AT.plusMinutes(2)));

		List<OrderHistoryEvent> events = orderHistoryRepositoryPort.findByOrderId(order.id());

		assertEquals(3, events.size());
		assertEquals(OrderHistoryEventType.ORDER_CREATED, events.get(0).eventType());
		assertNull(events.get(0).previousStatus());
		assertEquals(OrderHistoryEventType.ORDER_CONFIRMED, events.get(1).eventType());
		assertEquals(OrderHistoryEventType.ORDER_CANCELLED, events.get(2).eventType());
		assertEquals("Customer requested cancellation", events.get(2).description());
		assertEquals(CREATED_AT, events.get(0).createdAt());
		assertEquals(CREATED_AT.plusMinutes(2), events.get(2).createdAt());
	}

	private static Order sampleOrder(String code) {
		return Order.create(
				UUID.randomUUID(),
				OrderCode.of(code),
				"Ana Torres",
				"999999999",
				"Av. Example 123",
				new Location(-12.1001, -77.0201),
				Money.of(new BigDecimal("85.90")),
				CREATED_AT,
				CREATED_AT.plusMinutes(45));
	}
}
