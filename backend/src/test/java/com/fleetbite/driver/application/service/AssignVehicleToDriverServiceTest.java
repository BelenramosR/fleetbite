package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.AssignVehicleToDriverCommand;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserRole;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignVehicleToDriverServiceTest {

	private static final OffsetDateTime NOW =
			OffsetDateTime.of(2026, 8, 10, 12, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Location RESTAURANT = new Location(-12.0919738, -76.9737017);

	@Mock DriverRepositoryPort drivers;
	@Mock VehicleRepositoryPort vehicles;
	@Mock UserRepositoryPort users;

	@Test
	void assign_shouldInitializeDriverLocationAtRestaurant() {
		UUID userId = UUID.randomUUID();
		UUID driverId = UUID.randomUUID();
		UUID vehicleId = UUID.randomUUID();
		Driver driver = Driver.create(driverId, userId, "999888777", null, NOW);
		Vehicle vehicle = Vehicle.create(vehicleId, "MOT-123", VehicleType.MOTORCYCLE, NOW);
		User user = User.create(userId, "driver@fleetbite.pe", "hashed-password", "Driver Demo",
				UserRole.DRIVER, NOW);

		when(drivers.findById(driverId)).thenReturn(Optional.of(driver));
		when(vehicles.findById(vehicleId)).thenReturn(Optional.of(vehicle));
		when(drivers.findByVehicleId(vehicleId)).thenReturn(Optional.empty());
		when(vehicles.update(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(drivers.update(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(users.findById(userId)).thenReturn(Optional.of(user));

		AssignVehicleToDriverService service = new AssignVehicleToDriverService(
				drivers, vehicles, users, RESTAURANT,
				Clock.fixed(NOW.toInstant(), BusinessTime.ZONE_OFFSET));
		service.execute(driverId, new AssignVehicleToDriverCommand(vehicleId));

		ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
		verify(drivers).update(driverCaptor.capture());
		assertEquals(RESTAURANT, driverCaptor.getValue().currentLocation());
		assertEquals(vehicleId, driverCaptor.getValue().vehicleId());
	}
}
