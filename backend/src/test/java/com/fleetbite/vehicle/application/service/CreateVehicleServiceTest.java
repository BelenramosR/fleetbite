package com.fleetbite.vehicle.application.service;

import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.dto.CreateVehicleCommand;
import com.fleetbite.vehicle.application.dto.VehicleResult;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.exception.DuplicateVehiclePlateException;
import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleStatus;
import com.fleetbite.vehicle.domain.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateVehicleServiceTest {

	private static final OffsetDateTime FIXED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Clock FIXED_CLOCK = Clock.fixed(FIXED.toInstant(), BusinessTime.ZONE_OFFSET);

	@Mock
	private VehicleRepositoryPort vehicleRepositoryPort;

	private CreateVehicleService createVehicleService;

	@BeforeEach
	void setUp() {
		createVehicleService = new CreateVehicleService(vehicleRepositoryPort, FIXED_CLOCK);
	}

	@Test
	void execute_shouldCreateActiveVehicle() {
		when(vehicleRepositoryPort.existsByPlate("ABC-123")).thenReturn(false);
		when(vehicleRepositoryPort.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

		VehicleResult result = createVehicleService.execute(
				new CreateVehicleCommand("ABC-123", VehicleType.MOTORCYCLE));

		ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
		verify(vehicleRepositoryPort).save(captor.capture());
		Vehicle saved = captor.getValue();

		assertEquals(VehicleStatus.AVAILABLE, saved.status());
		assertEquals(FIXED, saved.createdAt());
		assertEquals("ABC-123", result.plate());
		assertEquals(VehicleType.MOTORCYCLE, result.type());
	}

	@Test
	void execute_shouldRejectDuplicatePlate() {
		when(vehicleRepositoryPort.existsByPlate("ABC-123")).thenReturn(true);

		assertThrows(
				DuplicateVehiclePlateException.class,
				() -> createVehicleService.execute(new CreateVehicleCommand("ABC-123", VehicleType.CAR)));

		verify(vehicleRepositoryPort, never()).save(any());
	}
}
