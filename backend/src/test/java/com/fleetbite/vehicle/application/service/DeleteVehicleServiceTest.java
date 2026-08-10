package com.fleetbite.vehicle.application.service;

import java.util.UUID;

import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.exception.VehicleNotDeletableException;
import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteVehicleServiceTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);

	@Mock
	private VehicleRepositoryPort vehicleRepositoryPort;

	@Mock
	private DriverRepositoryPort driverRepositoryPort;

	private DeleteVehicleService deleteVehicleService;

	@BeforeEach
	void setUp() {
		deleteVehicleService = new DeleteVehicleService(vehicleRepositoryPort, driverRepositoryPort);
	}

	@Test
	void execute_shouldDeleteInactiveVehicle() {
		Vehicle vehicle = Vehicle.create(UUID.randomUUID(), "ABC-123", VehicleType.MOTORCYCLE, CREATED);
		vehicle.deactivate(CREATED.plusMinutes(1));
		when(vehicleRepositoryPort.findById(vehicle.id())).thenReturn(Optional.of(vehicle));
		when(driverRepositoryPort.findByVehicleId(vehicle.id())).thenReturn(Optional.empty());

		deleteVehicleService.execute(vehicle.id());

		verify(vehicleRepositoryPort).deleteById(vehicle.id());
	}

	@Test
	void execute_shouldRejectAvailableVehicle() {
		Vehicle vehicle = Vehicle.create(UUID.randomUUID(), "ABC-123", VehicleType.MOTORCYCLE, CREATED);
		when(vehicleRepositoryPort.findById(vehicle.id())).thenReturn(Optional.of(vehicle));
		when(driverRepositoryPort.findByVehicleId(vehicle.id())).thenReturn(Optional.empty());

		assertThrows(VehicleNotDeletableException.class, () -> deleteVehicleService.execute(vehicle.id()));
		verify(vehicleRepositoryPort, never()).deleteById(vehicle.id());
	}

	@Test
	void execute_shouldThrowNotFound() {
		UUID id = UUID.randomUUID();
		when(vehicleRepositoryPort.findById(id)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> deleteVehicleService.execute(id));
	}
}
