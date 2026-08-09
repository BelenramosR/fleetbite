package com.fleetbite.vehicle.application.service;

import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.dto.UpdateVehicleCommand;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.exception.DuplicateVehiclePlateException;
import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleId;
import com.fleetbite.vehicle.domain.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateVehicleServiceTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime UPDATED =
			OffsetDateTime.of(2026, 8, 8, 23, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Clock FIXED_CLOCK = Clock.fixed(UPDATED.toInstant(), BusinessTime.ZONE_OFFSET);

	@Mock
	private VehicleRepositoryPort vehicleRepositoryPort;

	private UpdateVehicleService updateVehicleService;

	@BeforeEach
	void setUp() {
		updateVehicleService = new UpdateVehicleService(vehicleRepositoryPort, FIXED_CLOCK);
	}

	@Test
	void execute_shouldUpdateDetails() {
		Vehicle vehicle = Vehicle.create(VehicleId.generate(), "ABC-123", VehicleType.MOTORCYCLE, CREATED);
		when(vehicleRepositoryPort.findById(vehicle.id())).thenReturn(Optional.of(vehicle));
		when(vehicleRepositoryPort.existsByPlateAndIdNot("XYZ-999", vehicle.id())).thenReturn(false);
		when(vehicleRepositoryPort.update(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = updateVehicleService.execute(
				vehicle.id(),
				new UpdateVehicleCommand("XYZ-999", VehicleType.CAR));

		assertEquals("XYZ-999", result.plate());
		assertEquals(VehicleType.CAR, result.type());
		assertEquals(UPDATED, result.updatedAt());
		verify(vehicleRepositoryPort).update(vehicle);
	}

	@Test
	void execute_shouldRejectDuplicatePlate() {
		Vehicle vehicle = Vehicle.create(VehicleId.generate(), "ABC-123", VehicleType.MOTORCYCLE, CREATED);
		when(vehicleRepositoryPort.findById(vehicle.id())).thenReturn(Optional.of(vehicle));
		when(vehicleRepositoryPort.existsByPlateAndIdNot("XYZ-999", vehicle.id())).thenReturn(true);

		assertThrows(
				DuplicateVehiclePlateException.class,
				() -> updateVehicleService.execute(
						vehicle.id(),
						new UpdateVehicleCommand("XYZ-999", VehicleType.CAR)));
	}

	@Test
	void execute_shouldThrowNotFound() {
		VehicleId id = VehicleId.generate();
		when(vehicleRepositoryPort.findById(id)).thenReturn(Optional.empty());

		assertThrows(
				ResourceNotFoundException.class,
				() -> updateVehicleService.execute(id, new UpdateVehicleCommand("XYZ-999", VehicleType.CAR)));
	}
}
