package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.exception.InvalidDriverTransitionException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetDriverOnlineServiceTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime NOW =
			OffsetDateTime.of(2026, 8, 8, 22, 15, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Clock FIXED_CLOCK = Clock.fixed(NOW.toInstant(), BusinessTime.ZONE_OFFSET);

	@Mock
	private DriverRepositoryPort driverRepositoryPort;

	private SetDriverOnlineService setDriverOnlineService;

	@BeforeEach
	void setUp() {
		setDriverOnlineService = new SetDriverOnlineService(driverRepositoryPort, FIXED_CLOCK);
	}

	@Test
	void execute_shouldSetAvailableWhenLocationPresent() {
		Driver driver = Driver.create(
				DriverId.generate(),
				"Carlos Perez",
				"999888777",
				new Location(-12.10, -77.03),
				CREATED);
		when(driverRepositoryPort.findById(driver.id())).thenReturn(Optional.of(driver));
		when(driverRepositoryPort.update(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = setDriverOnlineService.execute(driver.id());

		assertEquals(DriverStatus.AVAILABLE, result.status());
		assertEquals(NOW, result.updatedAt());
		verify(driverRepositoryPort).update(driver);
	}

	@Test
	void execute_shouldRejectWithoutLocation() {
		Driver driver = Driver.create(DriverId.generate(), "Carlos Perez", "999888777", null, CREATED);
		when(driverRepositoryPort.findById(driver.id())).thenReturn(Optional.of(driver));

		assertThrows(InvalidDriverTransitionException.class, () -> setDriverOnlineService.execute(driver.id()));
	}
}
