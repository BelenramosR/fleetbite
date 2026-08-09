package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.CreateDriverCommand;
import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.exception.DuplicateDriverPhoneException;
import com.fleetbite.driver.domain.exception.InvalidDriverDataException;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDriverServiceTest {

	private static final OffsetDateTime FIXED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Clock FIXED_CLOCK = Clock.fixed(FIXED.toInstant(), BusinessTime.ZONE_OFFSET);

	@Mock
	private DriverRepositoryPort driverRepositoryPort;

	private CreateDriverService createDriverService;

	@BeforeEach
	void setUp() {
		createDriverService = new CreateDriverService(driverRepositoryPort, FIXED_CLOCK);
	}

	@Test
	void execute_shouldCreateOfflineDriverWithoutLocation() {
		when(driverRepositoryPort.existsByPhone("999888777")).thenReturn(false);
		when(driverRepositoryPort.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

		DriverResult result = createDriverService.execute(
				new CreateDriverCommand("Carlos Perez", "999888777", null, null));

		ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
		verify(driverRepositoryPort).save(captor.capture());
		Driver saved = captor.getValue();

		assertEquals(DriverStatus.OFFLINE, saved.status());
		assertNull(saved.currentLocation());
		assertEquals(FIXED, saved.createdAt());
		assertEquals(FIXED, result.createdAt());
		assertNull(result.currentLatitude());
	}

	@Test
	void execute_shouldCreateWithOptionalLocation() {
		when(driverRepositoryPort.existsByPhone(anyString())).thenReturn(false);
		when(driverRepositoryPort.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

		DriverResult result = createDriverService.execute(
				new CreateDriverCommand("Carlos Perez", "999888777", -12.10, -77.03));

		assertEquals(-12.10, result.currentLatitude());
		assertEquals(-77.03, result.currentLongitude());
		assertEquals(DriverStatus.OFFLINE, result.status());
	}

	@Test
	void execute_shouldRejectDuplicatePhone() {
		when(driverRepositoryPort.existsByPhone("999888777")).thenReturn(true);

		assertThrows(
				DuplicateDriverPhoneException.class,
				() -> createDriverService.execute(
						new CreateDriverCommand("Carlos Perez", "999888777", null, null)));

		verify(driverRepositoryPort, never()).save(any());
	}

	@Test
	void execute_shouldRejectPartialLocation() {
		when(driverRepositoryPort.existsByPhone(anyString())).thenReturn(false);

		assertThrows(
				InvalidDriverDataException.class,
				() -> createDriverService.execute(
						new CreateDriverCommand("Carlos Perez", "999888777", -12.10, null)));

		verify(driverRepositoryPort, never()).save(any());
	}
}
