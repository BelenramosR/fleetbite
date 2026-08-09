package com.fleetbite.order.infrastructure.outbound.persistence;

import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderId;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
		"spring.jpa.hibernate.ddl-auto=validate",
		"spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({OrderRepositoryAdapter.class, OrderPersistenceMapper.class})
@Testcontainers
class OrderRepositoryAdapterIntegrationTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime PROMISED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 45, 0, 0, BusinessTime.ZONE_OFFSET);

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

	@Autowired
	private OrderRepositoryPort orderRepositoryPort;

	@Test
	void saveAndFindById_shouldPersistAndReconstituteOrderWithBusinessOffset() {
		Order order = Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-PERSIST1"),
				"Ana Torres",
				"999999999",
				"Av. Example 123",
				new Location(-12.1001, -77.0201),
				Money.of(new BigDecimal("85.90")),
				CREATED_AT,
				PROMISED_AT);

		Order saved = orderRepositoryPort.save(order);
		Optional<Order> loaded = orderRepositoryPort.findById(saved.id());

		assertTrue(loaded.isPresent());
		Order found = loaded.get();
		assertEquals(saved.id(), found.id());
		assertEquals(OrderStatus.CREATED, found.status());
		assertEquals(BusinessTime.ZONE_OFFSET, found.createdAt().getOffset());
		assertEquals(BusinessTime.ZONE_OFFSET, found.promisedDeliveryAt().getOffset());
		assertEquals(CREATED_AT, found.createdAt());
		assertEquals(PROMISED_AT, found.promisedDeliveryAt());
	}

	@Test
	void findById_shouldReturnEmptyWhenMissing() {
		Optional<Order> loaded = orderRepositoryPort.findById(OrderId.generate());
		assertTrue(loaded.isEmpty());
	}
}
