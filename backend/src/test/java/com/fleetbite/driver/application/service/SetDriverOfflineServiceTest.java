package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.shared.domain.model.Location;
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
class SetDriverOfflineServiceTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime NOW =
			OffsetDateTime.of(2026, 8, 8, 22, 20, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Clock FIXED_CLOCK = Clock.fixed(NOW.toInstant(), BusinessTime.ZONE_OFFSET);

	@Mock
	private DriverRepositoryPort driverRepositoryPort;

	private SetDriverOfflineService setDriverOfflineService;

	@BeforeEach
	void setUp() {
		setDriverOfflineService = new SetDriverOfflineService(driverRepositoryPort, FIXED_CLOCK);
	}

	@Test
	void execute_shouldSetOfflineFromAvailable() {
		Driver driver = Driver.create(
				DriverId.generate(),
				"Carlos Perez",
				"999888777",
				new Location(-12.10, -77.03),
				CREATED);
		driver.goOnline(CREATED.plusMinutes(1));
		when(driverRepositoryPort.findById(driver.id())).thenReturn(Optional.of(driver));
		when(driverRepositoryPort.update(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = setDriverOfflineService.execute(driver.id());

		assertEquals(DriverStatus.OFFLINE, result.status());
		assertEquals(NOW, result.updatedAt());
	}
}
