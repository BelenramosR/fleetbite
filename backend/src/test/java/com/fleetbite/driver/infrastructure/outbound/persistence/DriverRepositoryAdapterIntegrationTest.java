package com.fleetbite.driver.infrastructure.outbound.persistence;

import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
		"spring.jpa.hibernate.ddl-auto=validate",
		"spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DriverRepositoryAdapter.class, DriverPersistenceMapper.class})
@Testcontainers
class DriverRepositoryAdapterIntegrationTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

	@Autowired
	private DriverRepositoryPort driverRepositoryPort;

	@Autowired
	private SpringDataDriverRepository springDataDriverRepository;

	@Test
	void saveAndFindById_shouldPersistAndReconstituteDriverWithBusinessOffset() {
		Driver saved = driverRepositoryPort.save(sampleDriver("999888777", null));
		Optional<Driver> loaded = driverRepositoryPort.findById(saved.id());

		assertTrue(loaded.isPresent());
		Driver found = loaded.get();
		assertEquals(saved.id(), found.id());
		assertEquals(DriverStatus.OFFLINE, found.status());
		assertEquals(CREATED_AT, found.createdAt());
		assertEquals(CREATED_AT, found.updatedAt());
	}

	@Test
	void findById_shouldReturnEmptyWhenMissing() {
		assertTrue(driverRepositoryPort.findById(DriverId.generate()).isEmpty());
	}

	@Test
	void findAll_shouldReturnDriversOrderedByCreatedAt() {
		Driver first = driverRepositoryPort.save(sampleDriver("111111111", null));
		Driver second = Driver.create(
				DriverId.generate(),
				"Luis Gomez",
				"222222222",
				null,
				CREATED_AT.plusMinutes(1));
		driverRepositoryPort.save(second);

		List<Driver> all = driverRepositoryPort.findAll();

		assertEquals(2, all.size());
		assertEquals(first.id(), all.get(0).id());
		assertEquals(second.id(), all.get(1).id());
	}

	@Test
	void update_shouldModifyExistingRowAndIncrementVersion() {
		Driver saved = driverRepositoryPort.save(sampleDriver("333333333", new Location(-12.10, -77.03)));
		Long versionBefore = springDataDriverRepository.findById(saved.id().value()).orElseThrow().getVersion();

		saved.updateProfile("Luis Gomez", "333333333", CREATED_AT.plusMinutes(5));
		saved.goOnline(CREATED_AT.plusMinutes(6));
		Driver updated = driverRepositoryPort.update(saved);

		assertEquals("Luis Gomez", updated.name());
		assertEquals(DriverStatus.AVAILABLE, updated.status());
		assertEquals(1, springDataDriverRepository.count());
		Long versionAfter = springDataDriverRepository.findById(saved.id().value()).orElseThrow().getVersion();
		assertEquals(versionBefore + 1, versionAfter);
	}

	@Test
	void deleteById_shouldRemoveRow() {
		Driver saved = driverRepositoryPort.save(sampleDriver("444444444", null));

		driverRepositoryPort.deleteById(saved.id());

		assertTrue(driverRepositoryPort.findById(saved.id()).isEmpty());
		assertEquals(0, springDataDriverRepository.count());
	}

	@Test
	void save_shouldEnforceUniquePhone() {
		driverRepositoryPort.save(sampleDriver("555555555", null));
		springDataDriverRepository.flush();

		assertThrows(DataIntegrityViolationException.class, () -> {
			driverRepositoryPort.save(sampleDriver("555555555", null));
			springDataDriverRepository.flush();
		});
	}

	@Test
	void existsByPhone_shouldDetectDuplicates() {
		driverRepositoryPort.save(sampleDriver("666666666", null));

		assertTrue(driverRepositoryPort.existsByPhone("666666666"));
	}

	private static Driver sampleDriver(String phone, Location location) {
		return Driver.create(DriverId.generate(), "Carlos Perez", phone, location, CREATED_AT);
	}
}
