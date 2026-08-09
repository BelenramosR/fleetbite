package com.fleetbite.delivery.infrastructure.outbound.persistence;

import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.model.AssignmentStatus;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.delivery.domain.model.DeliveryAssignmentId;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.driver.infrastructure.outbound.persistence.DriverPersistenceMapperImpl;
import com.fleetbite.driver.infrastructure.outbound.persistence.DriverRepositoryAdapter;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.order.infrastructure.outbound.persistence.OrderPersistenceMapperImpl;
import com.fleetbite.order.infrastructure.outbound.persistence.OrderRepositoryAdapter;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
		"spring.jpa.hibernate.ddl-auto=validate",
		"spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
		DeliveryAssignmentRepositoryAdapter.class,
		DeliveryAssignmentPersistenceMapperImpl.class,
		OrderRepositoryAdapter.class,
		OrderPersistenceMapperImpl.class,
		DriverRepositoryAdapter.class,
		DriverPersistenceMapperImpl.class
})
@Testcontainers
class DeliveryAssignmentRepositoryAdapterIntegrationTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

	@Autowired
	private DeliveryAssignmentRepositoryPort assignmentRepositoryPort;
	@Autowired
	private OrderRepositoryPort orderRepositoryPort;
	@Autowired
	private DriverRepositoryPort driverRepositoryPort;
	@Autowired
	private SpringDataDeliveryAssignmentRepository springDataDeliveryAssignmentRepository;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void saveAndFindById_shouldPersistAssignment() {
		Order order = orderRepositoryPort.save(sampleOrder("ORD-2026-ASGSAVE"));
		Driver driver = driverRepositoryPort.save(sampleDriver("700000001"));

		DeliveryAssignment saved = assignmentRepositoryPort.save(DeliveryAssignment.create(
				DeliveryAssignmentId.generate(),
				order.id(),
				driver.id(),
				CREATED));

		assertTrue(assignmentRepositoryPort.findById(saved.id()).isPresent());
		assertEquals(AssignmentStatus.PENDING, saved.status());
		assertEquals(CREATED, assignmentRepositoryPort.findById(saved.id()).orElseThrow().assignedAt());
	}

	@Test
	void update_shouldIncrementVersion() {
		Order order = orderRepositoryPort.save(sampleOrder("ORD-2026-ASGUPD"));
		Driver driver = driverRepositoryPort.save(sampleDriver("700000002"));
		DeliveryAssignment saved = assignmentRepositoryPort.save(DeliveryAssignment.create(
				DeliveryAssignmentId.generate(),
				order.id(),
				driver.id(),
				CREATED));
		Long versionBefore = springDataDeliveryAssignmentRepository.findById(saved.id().value())
				.orElseThrow()
				.getVersion();

		saved.accept(CREATED.plusMinutes(1));
		assignmentRepositoryPort.update(saved);
		springDataDeliveryAssignmentRepository.flush();

		Long versionAfter = springDataDeliveryAssignmentRepository.findById(saved.id().value())
				.orElseThrow()
				.getVersion();
		assertEquals(versionBefore + 1, versionAfter);
	}

	@Test
	void existsActiveByOrderId_shouldDetectPending() {
		Order order = orderRepositoryPort.save(sampleOrder("ORD-2026-ASGACT"));
		Driver driver = driverRepositoryPort.save(sampleDriver("700000003"));
		assignmentRepositoryPort.save(DeliveryAssignment.create(
				DeliveryAssignmentId.generate(),
				order.id(),
				driver.id(),
				CREATED));

		assertTrue(assignmentRepositoryPort.existsActiveByOrderId(order.id()));
	}

	@Test
	void save_shouldEnforceOneActiveAssignmentPerOrder() {
		Order order = orderRepositoryPort.save(sampleOrder("ORD-2026-ASGUNI"));
		Driver driver1 = driverRepositoryPort.save(sampleDriver("700000004"));
		Driver driver2 = driverRepositoryPort.save(sampleDriver("700000005"));
		assignmentRepositoryPort.save(DeliveryAssignment.create(
				DeliveryAssignmentId.generate(),
				order.id(),
				driver1.id(),
				CREATED));
		springDataDeliveryAssignmentRepository.flush();

		assertThrows(DataIntegrityViolationException.class, () -> {
			assignmentRepositoryPort.save(DeliveryAssignment.create(
					DeliveryAssignmentId.generate(),
					order.id(),
					driver2.id(),
					CREATED.plusMinutes(1)));
			springDataDeliveryAssignmentRepository.flush();
		});
	}

	@Test
	void findAll_shouldReturnCreatedAssignments() {
		Order order = orderRepositoryPort.save(sampleOrder("ORD-2026-ASGLST"));
		Driver driver = driverRepositoryPort.save(sampleDriver("700000006"));
		assignmentRepositoryPort.save(DeliveryAssignment.create(
				DeliveryAssignmentId.generate(),
				order.id(),
				driver.id(),
				CREATED));

		assertEquals(1, assignmentRepositoryPort.findAll().size());
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
				CREATED,
				CREATED.plusMinutes(45));
	}

	private Driver sampleDriver(String phone) {
		return Driver.create(
				DriverId.generate(),
				insertDriverUser(),
				phone,
				new Location(-12.10, -77.03),
				CREATED);
	}

	private UserId insertDriverUser() {
		UUID id = UUID.randomUUID();
		jdbcTemplate.update(
				"""
						INSERT INTO users (id, email, password_hash, full_name, role, status, created_at, updated_at, version)
						VALUES (?, ?, 'hash', 'Test Driver', 'DRIVER', 'ACTIVE', ?, ?, 0)
						""",
				id,
				"driver-" + id + "@test.local",
				CREATED,
				CREATED);
		return UserId.of(id);
	}
}
