package com.fleetbite.driver.infrastructure.outbound.persistence;

import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void saveAndFindById_shouldPersistAndReconstituteDriverWithBusinessOffset() {
		Driver saved = driverRepositoryPort.save(sampleDriver("999888777", null));
		Optional<Driver> loaded = driverRepositoryPort.findById(saved.id());

		assertTrue(loaded.isPresent());
		Driver found = loaded.get();
		assertEquals(saved.id(), found.id());
		assertEquals(saved.userId(), found.userId());
		assertEquals(DriverStatus.OFFLINE, found.status());
		assertEquals(CREATED_AT, found.createdAt());
		assertEquals(CREATED_AT, found.updatedAt());
	}

	@Test
	void findById_shouldReturnEmptyWhenMissing() {
		assertTrue(driverRepositoryPort.findById(UUID.randomUUID()).isEmpty());
	}

	@Test
	void findAll_shouldReturnDriversOrderedByCreatedAt() {
		long baseline = springDataDriverRepository.count();
		Driver first = driverRepositoryPort.save(sampleDriver("111111111", null));
		Driver second = Driver.create(
				UUID.randomUUID(),
				insertDriverUser(),
				"222222222",
				null,
				CREATED_AT.plusMinutes(1));
		driverRepositoryPort.save(second);

		List<Driver> all = driverRepositoryPort.findAll();

		assertEquals(baseline + 2, all.size());
		int firstIndex = indexOf(all, first.id());
		int secondIndex = indexOf(all, second.id());
		assertTrue(firstIndex >= 0);
		assertTrue(secondIndex > firstIndex);
	}

	@Test
	void update_shouldModifyExistingRowAndIncrementVersion() {
		long baseline = springDataDriverRepository.count();
		Driver saved = driverRepositoryPort.save(sampleDriver("333333333", new Location(-12.10, -77.03)));
		Long versionBefore = springDataDriverRepository.findById(saved.id()).orElseThrow().getVersion();

		saved.updatePhone("333333333", CREATED_AT.plusMinutes(5));
		saved.goOnline(CREATED_AT.plusMinutes(6));
		Driver updated = driverRepositoryPort.update(saved);

		assertEquals("333333333", updated.phone());
		assertEquals(DriverStatus.AVAILABLE, updated.status());
		assertEquals(baseline + 1, springDataDriverRepository.count());
		Long versionAfter = springDataDriverRepository.findById(saved.id()).orElseThrow().getVersion();
		assertEquals(versionBefore + 1, versionAfter);
	}

	@Test
	void deleteById_shouldRemoveRow() {
		long baseline = springDataDriverRepository.count();
		Driver saved = driverRepositoryPort.save(sampleDriver("444444444", null));

		driverRepositoryPort.deleteById(saved.id());

		assertTrue(driverRepositoryPort.findById(saved.id()).isEmpty());
		assertEquals(baseline, springDataDriverRepository.count());
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

	@Test
	void findAvailableWithLocation_shouldReturnOnlyAvailableDriversWithVehicleAndCoordinates() {
		UUID vehicleId = insertVehicle("AVA-001");
		Driver available = sampleDriver("777777777", new Location(-12.10, -77.03));
		available.assignVehicle(vehicleId, CREATED_AT.plusSeconds(30));
		available.goOnline(CREATED_AT.plusMinutes(1));
		driverRepositoryPort.save(available);

		Driver offlineWithLocation = sampleDriver("888888888", new Location(-12.11, -77.04));
		driverRepositoryPort.save(offlineWithLocation);

		Driver offlineWithoutLocation = sampleDriver("999999999", null);
		driverRepositoryPort.save(offlineWithoutLocation);

		UUID busyVehicleId = insertVehicle("BSY-001");
		Driver busy = sampleDriver("101010101", new Location(-12.12, -77.05));
		busy.assignVehicle(busyVehicleId, CREATED_AT.plusSeconds(30));
		busy.goOnline(CREATED_AT.plusMinutes(1));
		busy.markBusy(CREATED_AT.plusMinutes(2));
		driverRepositoryPort.save(busy);

		List<Driver> found = driverRepositoryPort.findAvailableWithLocation();

		assertEquals(1, found.size());
		assertEquals(available.id(), found.getFirst().id());
		assertEquals(DriverStatus.AVAILABLE, found.getFirst().status());
		assertEquals(vehicleId, found.getFirst().vehicleId());
	}

	private Driver sampleDriver(String phone, Location location) {
		return Driver.create(UUID.randomUUID(), insertDriverUser(), phone, location, CREATED_AT);
	}

	private static int indexOf(List<Driver> drivers, UUID id) {
		for (int i = 0; i < drivers.size(); i++) {
			if (drivers.get(i).id().equals(id)) {
				return i;
			}
		}
		return -1;
	}

	private UUID insertDriverUser() {
		UUID id = UUID.randomUUID();
		jdbcTemplate.update(
				"""
						INSERT INTO users (id, email, password_hash, full_name, role, status, created_at, updated_at, version)
						VALUES (?, ?, 'hash', 'Test Driver', 'DRIVER', 'ACTIVE', ?, ?, 0)
						""",
				id,
				"driver-" + id + "@test.local",
				CREATED_AT,
				CREATED_AT);
		return id;
	}

	private UUID insertVehicle(String plate) {
		UUID id = UUID.randomUUID();
		jdbcTemplate.update(
				"""
						INSERT INTO vehicles (id, plate, type, status, created_at, updated_at, version)
						VALUES (?, ?, 'MOTORCYCLE', 'AVAILABLE', ?, ?, 0)
						""",
				id,
				plate,
				CREATED_AT,
				CREATED_AT);
		return id;
	}
}
