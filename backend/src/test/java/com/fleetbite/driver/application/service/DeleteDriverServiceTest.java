package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.exception.DriverNotDeletableException;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
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
class DeleteDriverServiceTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);

	@Mock
	private DriverRepositoryPort driverRepositoryPort;

	private DeleteDriverService deleteDriverService;

	@BeforeEach
	void setUp() {
		deleteDriverService = new DeleteDriverService(driverRepositoryPort);
	}

	@Test
	void execute_shouldDeleteOfflineDriver() {
		Driver driver = Driver.create(DriverId.generate(), "Carlos Perez", "999888777", null, CREATED);
		when(driverRepositoryPort.findById(driver.id())).thenReturn(Optional.of(driver));

		deleteDriverService.execute(driver.id());

		verify(driverRepositoryPort).deleteById(driver.id());
	}

	@Test
	void execute_shouldRejectAvailableDriver() {
		Driver driver = Driver.create(
				DriverId.generate(),
				"Carlos Perez",
				"999888777",
				new Location(-12.10, -77.03),
				CREATED);
		driver.goOnline(CREATED.plusMinutes(1));
		when(driverRepositoryPort.findById(driver.id())).thenReturn(Optional.of(driver));

		assertThrows(DriverNotDeletableException.class, () -> deleteDriverService.execute(driver.id()));
		verify(driverRepositoryPort, never()).deleteById(driver.id());
	}

	@Test
	void execute_shouldThrowNotFound() {
		DriverId id = DriverId.generate();
		when(driverRepositoryPort.findById(id)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> deleteDriverService.execute(id));
	}
}
