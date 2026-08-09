package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.UpdateDriverLocationCommand;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.identity.domain.model.UserRole;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
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
class UpdateDriverLocationServiceTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime NOW =
			OffsetDateTime.of(2026, 8, 8, 22, 10, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Clock FIXED_CLOCK = Clock.fixed(NOW.toInstant(), BusinessTime.ZONE_OFFSET);
	private static final UserId USER_ID = UserId.generate();

	@Mock
	private DriverRepositoryPort driverRepositoryPort;

	@Mock
	private UserRepositoryPort userRepositoryPort;

	@Mock
	private VehicleRepositoryPort vehicleRepositoryPort;

	private UpdateDriverLocationService updateDriverLocationService;

	@BeforeEach
	void setUp() {
		updateDriverLocationService = new UpdateDriverLocationService(
				driverRepositoryPort,
				userRepositoryPort,
				vehicleRepositoryPort,
				FIXED_CLOCK);
	}

	@Test
	void execute_shouldUpdateLocation() {
		Driver driver = Driver.create(DriverId.generate(), USER_ID, "999888777", null, CREATED);
		when(driverRepositoryPort.findById(driver.id())).thenReturn(Optional.of(driver));
		when(driverRepositoryPort.update(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(driverUser()));

		var result = updateDriverLocationService.execute(
				driver.id(),
				new UpdateDriverLocationCommand(-12.10, -77.03));

		assertEquals(-12.10, result.currentLatitude());
		assertEquals(-77.03, result.currentLongitude());
		assertEquals(NOW, result.updatedAt());
	}

	private static User driverUser() {
		return User.create(USER_ID, "driver@fleetbite.test", "hash", "Carlos Perez", UserRole.DRIVER, CREATED);
	}
}
