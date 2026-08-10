package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.exception.DriverAlreadyLinkedToUserException;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProvisionDriverProfileServiceTest {

	private static final OffsetDateTime FIXED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Clock FIXED_CLOCK = Clock.fixed(FIXED.toInstant(), BusinessTime.ZONE_OFFSET);
	private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Mock
	private DriverRepositoryPort driverRepositoryPort;

	private ProvisionDriverProfileService provisionDriverProfileService;

	@BeforeEach
	void setUp() {
		provisionDriverProfileService = new ProvisionDriverProfileService(driverRepositoryPort, FIXED_CLOCK);
	}

	@Test
	void provision_shouldCreateOfflineDriverWithoutPhoneOrLocation() {
		when(driverRepositoryPort.existsByUserId(USER_ID)).thenReturn(false);
		when(driverRepositoryPort.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

		provisionDriverProfileService.provisionForDriverUser(USER_ID);

		ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
		verify(driverRepositoryPort).save(captor.capture());
		Driver saved = captor.getValue();

		assertEquals(DriverStatus.OFFLINE, saved.status());
		assertEquals(USER_ID, saved.userId());
		assertNull(saved.phone());
		assertNull(saved.currentLocation());
		assertNull(saved.vehicleId());
		assertEquals(FIXED, saved.createdAt());
	}

	@Test
	void provision_shouldRejectAlreadyLinkedUser() {
		when(driverRepositoryPort.existsByUserId(USER_ID)).thenReturn(true);

		assertThrows(
				DriverAlreadyLinkedToUserException.class,
				() -> provisionDriverProfileService.provisionForDriverUser(USER_ID));

		verify(driverRepositoryPort, never()).save(any());
	}
}
