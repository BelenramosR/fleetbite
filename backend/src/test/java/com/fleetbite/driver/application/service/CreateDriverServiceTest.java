package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.CreateDriverCommand;
import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.exception.DriverAlreadyLinkedToUserException;
import com.fleetbite.driver.domain.exception.DriverUserNotEligibleException;
import com.fleetbite.driver.domain.exception.DuplicateDriverPhoneException;
import com.fleetbite.driver.domain.exception.InvalidDriverDataException;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.identity.domain.model.UserRole;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.BeforeEach;
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
	private static final UUID USER_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UserId USER_ID = UserId.of(USER_UUID);

	@Mock
	private UserRepositoryPort userRepositoryPort;

	@Mock
	private DriverRepositoryPort driverRepositoryPort;

	private CreateDriverService createDriverService;

	@BeforeEach
	void setUp() {
		createDriverService = new CreateDriverService(userRepositoryPort, driverRepositoryPort, FIXED_CLOCK);
	}

	@Test
	void execute_shouldCreateOfflineDriverWithoutLocation() {
		when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(driverUser()));
		when(driverRepositoryPort.existsByUserId(USER_ID)).thenReturn(false);
		when(driverRepositoryPort.existsByPhone("999888777")).thenReturn(false);
		when(driverRepositoryPort.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

		DriverResult result = createDriverService.execute(
				new CreateDriverCommand(USER_UUID, "999888777", null, null));

		ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
		verify(driverRepositoryPort).save(captor.capture());
		Driver saved = captor.getValue();

		assertEquals(DriverStatus.OFFLINE, saved.status());
		assertEquals(USER_ID, saved.userId());
		assertNull(saved.currentLocation());
		assertEquals(FIXED, saved.createdAt());
		assertEquals(FIXED, result.createdAt());
		assertEquals("Carlos Perez", result.name());
		assertEquals(USER_UUID, result.userId());
		assertNull(result.currentLatitude());
	}

	@Test
	void execute_shouldCreateWithOptionalLocation() {
		when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(driverUser()));
		when(driverRepositoryPort.existsByUserId(USER_ID)).thenReturn(false);
		when(driverRepositoryPort.existsByPhone(anyString())).thenReturn(false);
		when(driverRepositoryPort.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

		DriverResult result = createDriverService.execute(
				new CreateDriverCommand(USER_UUID, "999888777", -12.10, -77.03));

		assertEquals(-12.10, result.currentLatitude());
		assertEquals(-77.03, result.currentLongitude());
		assertEquals(DriverStatus.OFFLINE, result.status());
	}

	@Test
	void execute_shouldRejectMissingUser() {
		when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.empty());

		assertThrows(
				ResourceNotFoundException.class,
				() -> createDriverService.execute(
						new CreateDriverCommand(USER_UUID, "999888777", null, null)));

		verify(driverRepositoryPort, never()).save(any());
	}

	@Test
	void execute_shouldRejectNonDriverRole() {
		when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(adminUser()));

		assertThrows(
				DriverUserNotEligibleException.class,
				() -> createDriverService.execute(
						new CreateDriverCommand(USER_UUID, "999888777", null, null)));

		verify(driverRepositoryPort, never()).save(any());
	}

	@Test
	void execute_shouldRejectAlreadyLinkedUser() {
		when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(driverUser()));
		when(driverRepositoryPort.existsByUserId(USER_ID)).thenReturn(true);

		assertThrows(
				DriverAlreadyLinkedToUserException.class,
				() -> createDriverService.execute(
						new CreateDriverCommand(USER_UUID, "999888777", null, null)));

		verify(driverRepositoryPort, never()).save(any());
	}

	@Test
	void execute_shouldRejectDuplicatePhone() {
		when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(driverUser()));
		when(driverRepositoryPort.existsByUserId(USER_ID)).thenReturn(false);
		when(driverRepositoryPort.existsByPhone("999888777")).thenReturn(true);

		assertThrows(
				DuplicateDriverPhoneException.class,
				() -> createDriverService.execute(
						new CreateDriverCommand(USER_UUID, "999888777", null, null)));

		verify(driverRepositoryPort, never()).save(any());
	}

	@Test
	void execute_shouldRejectPartialLocation() {
		when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(driverUser()));
		when(driverRepositoryPort.existsByUserId(USER_ID)).thenReturn(false);
		when(driverRepositoryPort.existsByPhone(anyString())).thenReturn(false);

		assertThrows(
				InvalidDriverDataException.class,
				() -> createDriverService.execute(
						new CreateDriverCommand(USER_UUID, "999888777", -12.10, null)));

		verify(driverRepositoryPort, never()).save(any());
	}

	private static User driverUser() {
		return User.create(USER_ID, "driver@fleetbite.test", "hash", "Carlos Perez", UserRole.DRIVER, FIXED);
	}

	private static User adminUser() {
		return User.create(USER_ID, "admin@fleetbite.test", "hash", "Admin", UserRole.ADMIN, FIXED);
	}
}
