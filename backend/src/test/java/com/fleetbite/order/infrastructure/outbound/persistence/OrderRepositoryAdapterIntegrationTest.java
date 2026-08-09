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
import java.util.List;
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

	@Autowired
	private SpringDataOrderRepository springDataOrderRepository;

	@Test
	void saveAndFindById_shouldPersistAndReconstituteOrderWithBusinessOffset() {
		Order saved = orderRepositoryPort.save(sampleOrder("ORD-2026-PERSIST1"));
		Optional<Order> loaded = orderRepositoryPort.findById(saved.id());

		assertTrue(loaded.isPresent());
		Order found = loaded.get();
		assertEquals(saved.id(), found.id());
		assertEquals(OrderStatus.CREATED, found.status());
		assertEquals(CREATED_AT, found.createdAt());
		assertEquals(PROMISED_AT, found.promisedDeliveryAt());
	}

	@Test
	void findById_shouldReturnEmptyWhenMissing() {
		assertTrue(orderRepositoryPort.findById(OrderId.generate()).isEmpty());
	}

	@Test
	void findAll_shouldReturnOrdersOrderedByCreatedAt() {
		Order first = orderRepositoryPort.save(sampleOrder("ORD-2026-LIST0001"));
		Order second = Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-LIST0002"),
				"Luis Gomez",
				"988000111",
				"Calle Nueva 10",
				new Location(-12.11, -77.03),
				Money.of(new BigDecimal("40.00")),
				CREATED_AT.plusMinutes(1),
				PROMISED_AT.plusMinutes(1));
		orderRepositoryPort.save(second);

		List<Order> all = orderRepositoryPort.findAll();

		assertEquals(2, all.size());
		assertEquals(first.id(), all.get(0).id());
		assertEquals(second.id(), all.get(1).id());
	}

	@Test
	void update_shouldModifyExistingRowAndIncrementVersion() {
		Order saved = orderRepositoryPort.save(sampleOrder("ORD-2026-UPD00001"));
		Long versionBefore = springDataOrderRepository.findById(saved.id().value()).orElseThrow().getVersion();

		saved.updateDetails(
				"Luis Gomez",
				"988000111",
				"Calle Nueva 10",
				new Location(-12.11, -77.03),
				Money.of(new BigDecimal("40.00")));
		Order updated = orderRepositoryPort.update(saved);

		assertEquals("Luis Gomez", updated.customerName());
		assertEquals(0, updated.totalAmount().amount().compareTo(new BigDecimal("40.00")));
		assertEquals(1, springDataOrderRepository.count());
		Long versionAfter = springDataOrderRepository.findById(saved.id().value()).orElseThrow().getVersion();
		assertEquals(versionBefore + 1, versionAfter);
	}

	@Test
	void deleteById_shouldRemoveRow() {
		Order saved = orderRepositoryPort.save(sampleOrder("ORD-2026-DEL00001"));

		orderRepositoryPort.deleteById(saved.id());

		assertTrue(orderRepositoryPort.findById(saved.id()).isEmpty());
		assertEquals(0, springDataOrderRepository.count());
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
