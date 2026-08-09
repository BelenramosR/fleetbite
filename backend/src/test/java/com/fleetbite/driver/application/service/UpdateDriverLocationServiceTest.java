package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.UpdateDriverLocationCommand;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.shared.domain.time.BusinessTime;
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

	@Mock
	private DriverRepositoryPort driverRepositoryPort;

	private UpdateDriverLocationService updateDriverLocationService;

	@BeforeEach
	void setUp() {
		updateDriverLocationService = new UpdateDriverLocationService(driverRepositoryPort, FIXED_CLOCK);
	}

	@Test
	void execute_shouldUpdateLocation() {
		Driver driver = Driver.create(DriverId.generate(), "Carlos Perez", "999888777", null, CREATED);
		when(driverRepositoryPort.findById(driver.id())).thenReturn(Optional.of(driver));
		when(driverRepositoryPort.update(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = updateDriverLocationService.execute(
				driver.id(),
				new UpdateDriverLocationCommand(-12.10, -77.03));

		assertEquals(-12.10, result.currentLatitude());
		assertEquals(-77.03, result.currentLongitude());
		assertEquals(NOW, result.updatedAt());
	}
}
