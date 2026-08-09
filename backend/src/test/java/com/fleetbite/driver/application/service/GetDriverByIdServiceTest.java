package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.identity.domain.model.UserRole;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDriverByIdServiceTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final UserId USER_ID = UserId.generate();

	@Mock
	private DriverRepositoryPort driverRepositoryPort;

	@Mock
	private UserRepositoryPort userRepositoryPort;

	@Mock
	private VehicleRepositoryPort vehicleRepositoryPort;

	private GetDriverByIdService getDriverByIdService;

	@BeforeEach
	void setUp() {
		getDriverByIdService = new GetDriverByIdService(
				driverRepositoryPort,
				userRepositoryPort,
				vehicleRepositoryPort);
	}

	@Test
	void execute_shouldReturnResult() {
		Driver driver = Driver.create(DriverId.generate(), USER_ID, "999888777", null, CREATED);
		when(driverRepositoryPort.findById(driver.id())).thenReturn(Optional.of(driver));
		when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(driverUser()));

		var result = getDriverByIdService.execute(driver.id());

		assertEquals(driver.id().value(), result.id());
		assertEquals("Carlos Perez", result.name());
		assertEquals(USER_ID.value(), result.userId());
	}

	@Test
	void execute_shouldThrowNotFound() {
		DriverId id = DriverId.generate();
		when(driverRepositoryPort.findById(id)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> getDriverByIdService.execute(id));
	}

	private static User driverUser() {
		return User.create(USER_ID, "driver@fleetbite.test", "hash", "Carlos Perez", UserRole.DRIVER, CREATED);
	}
}
