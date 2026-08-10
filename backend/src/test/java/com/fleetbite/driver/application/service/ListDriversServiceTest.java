package com.fleetbite.driver.application.service;

import java.util.UUID;

import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserRole;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListDriversServiceTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final UUID USER_ID = UUID.randomUUID();

	@Mock
	private DriverRepositoryPort driverRepositoryPort;

	@Mock
	private UserRepositoryPort userRepositoryPort;

	@Mock
	private VehicleRepositoryPort vehicleRepositoryPort;

	private ListDriversService listDriversService;

	@BeforeEach
	void setUp() {
		listDriversService = new ListDriversService(
				driverRepositoryPort,
				userRepositoryPort,
				vehicleRepositoryPort);
	}

	@Test
	void execute_shouldMapAllDrivers() {
		Driver driver = Driver.create(UUID.randomUUID(), USER_ID, "999888777", null, CREATED);
		when(driverRepositoryPort.findAll()).thenReturn(List.of(driver));
		when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(driverUser()));

		var results = listDriversService.execute();

		assertEquals(1, results.size());
		assertEquals(driver.id(), results.getFirst().id());
		assertEquals("Carlos Perez", results.getFirst().name());
	}

	private static User driverUser() {
		return User.create(USER_ID, "driver@fleetbite.test", "hash", "Carlos Perez", UserRole.DRIVER, CREATED);
	}
}
