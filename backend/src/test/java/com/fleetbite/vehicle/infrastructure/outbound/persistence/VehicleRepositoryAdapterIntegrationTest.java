package com.fleetbite.vehicle.infrastructure.outbound.persistence;

import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleId;
import com.fleetbite.vehicle.domain.model.VehicleStatus;
import com.fleetbite.vehicle.domain.model.VehicleType;
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
@Import({VehicleRepositoryAdapter.class, VehiclePersistenceMapper.class})
@Testcontainers
class VehicleRepositoryAdapterIntegrationTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

	@Autowired
	private VehicleRepositoryPort vehicleRepositoryPort;

	@Autowired
	private SpringDataVehicleRepository springDataVehicleRepository;

	@Test
	void saveAndFindById_shouldPersistAndReconstituteWithBusinessOffset() {
		Vehicle saved = vehicleRepositoryPort.save(sampleVehicle("ABC-123"));
		Optional<Vehicle> loaded = vehicleRepositoryPort.findById(saved.id());

		assertTrue(loaded.isPresent());
		Vehicle found = loaded.get();
		assertEquals(saved.id(), found.id());
		assertEquals(VehicleStatus.AVAILABLE, found.status());
		assertEquals(CREATED_AT, found.createdAt());
		assertEquals(CREATED_AT, found.updatedAt());
	}

	@Test
	void findById_shouldReturnEmptyWhenMissing() {
		assertTrue(vehicleRepositoryPort.findById(VehicleId.generate()).isEmpty());
	}

	@Test
	void findAll_shouldReturnVehiclesOrderedByCreatedAt() {
		Vehicle first = vehicleRepositoryPort.save(sampleVehicle("AAA-111"));
		Vehicle second = Vehicle.create(
				VehicleId.generate(),
				"BBB-222",
				VehicleType.CAR,
				CREATED_AT.plusMinutes(1));
		vehicleRepositoryPort.save(second);

		List<Vehicle> all = vehicleRepositoryPort.findAll();

		assertEquals(2, all.size());
		assertEquals(first.id(), all.get(0).id());
		assertEquals(second.id(), all.get(1).id());
	}

	@Test
	void update_shouldModifyExistingRowAndIncrementVersion() {
		Vehicle saved = vehicleRepositoryPort.save(sampleVehicle("CCC-333"));
		Long versionBefore = springDataVehicleRepository.findById(saved.id().value()).orElseThrow().getVersion();

		saved.sendToMaintenance(CREATED_AT.plusMinutes(5));
		Vehicle updated = vehicleRepositoryPort.update(saved);

		assertEquals(VehicleStatus.MAINTENANCE, updated.status());
		assertEquals(1, springDataVehicleRepository.count());
		Long versionAfter = springDataVehicleRepository.findById(saved.id().value()).orElseThrow().getVersion();
		assertEquals(versionBefore + 1, versionAfter);
	}

	@Test
	void deleteById_shouldRemoveRow() {
		Vehicle saved = vehicleRepositoryPort.save(sampleVehicle("DDD-444"));

		vehicleRepositoryPort.deleteById(saved.id());

		assertTrue(vehicleRepositoryPort.findById(saved.id()).isEmpty());
		assertEquals(0, springDataVehicleRepository.count());
	}

	@Test
	void save_shouldEnforceUniquePlate() {
		vehicleRepositoryPort.save(sampleVehicle("EEE-555"));
		springDataVehicleRepository.flush();

		assertThrows(DataIntegrityViolationException.class, () -> {
			vehicleRepositoryPort.save(sampleVehicle("EEE-555"));
			springDataVehicleRepository.flush();
		});
	}

	@Test
	void existsByPlate_shouldDetectDuplicates() {
		vehicleRepositoryPort.save(sampleVehicle("FFF-666"));

		assertTrue(vehicleRepositoryPort.existsByPlate("FFF-666"));
	}

	private static Vehicle sampleVehicle(String plate) {
		return Vehicle.create(VehicleId.generate(), plate, VehicleType.MOTORCYCLE, CREATED_AT);
	}
}
