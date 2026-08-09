package com.fleetbite.vehicle.application.service;

import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleId;
import com.fleetbite.vehicle.domain.model.VehicleStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleStatusServicesTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime NOW =
			OffsetDateTime.of(2026, 8, 8, 22, 15, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Clock FIXED_CLOCK = Clock.fixed(NOW.toInstant(), BusinessTime.ZONE_OFFSET);

	@Mock
	private VehicleRepositoryPort vehicleRepositoryPort;

	private SendVehicleToMaintenanceService maintenanceService;
	private ActivateVehicleService activateService;
	private DeactivateVehicleService deactivateService;

	@BeforeEach
	void setUp() {
		maintenanceService = new SendVehicleToMaintenanceService(vehicleRepositoryPort, FIXED_CLOCK);
		activateService = new ActivateVehicleService(vehicleRepositoryPort, FIXED_CLOCK);
		deactivateService = new DeactivateVehicleService(vehicleRepositoryPort, FIXED_CLOCK);
	}

	@Test
	void maintenance_shouldSetMaintenance() {
		Vehicle vehicle = Vehicle.create(VehicleId.generate(), "ABC-123", VehicleType.MOTORCYCLE, CREATED);
		when(vehicleRepositoryPort.findById(vehicle.id())).thenReturn(Optional.of(vehicle));
		when(vehicleRepositoryPort.update(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = maintenanceService.execute(vehicle.id());

		assertEquals(VehicleStatus.MAINTENANCE, result.status());
		assertEquals(NOW, result.updatedAt());
	}

	@Test
	void activate_shouldSetActiveFromMaintenance() {
		Vehicle vehicle = Vehicle.create(VehicleId.generate(), "ABC-123", VehicleType.MOTORCYCLE, CREATED);
		vehicle.sendToMaintenance(CREATED.plusMinutes(1));
		when(vehicleRepositoryPort.findById(vehicle.id())).thenReturn(Optional.of(vehicle));
		when(vehicleRepositoryPort.update(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = activateService.execute(vehicle.id());

		assertEquals(VehicleStatus.AVAILABLE, result.status());
	}

	@Test
	void deactivate_shouldSetInactive() {
		Vehicle vehicle = Vehicle.create(VehicleId.generate(), "ABC-123", VehicleType.MOTORCYCLE, CREATED);
		when(vehicleRepositoryPort.findById(vehicle.id())).thenReturn(Optional.of(vehicle));
		when(vehicleRepositoryPort.update(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = deactivateService.execute(vehicle.id());

		assertEquals(VehicleStatus.INACTIVE, result.status());
	}
}
