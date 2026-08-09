package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.exception.DriverAlreadyLinkedToUserException;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.identity.application.port.out.DriverProfileProvisionerPort;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Provisions an empty Driver profile (no phone/location/vehicle) when a DRIVER user is created.
 */
public final class ProvisionDriverProfileService implements DriverProfileProvisionerPort {

	private final DriverRepositoryPort driverRepositoryPort;
	private final Clock clock;

	public ProvisionDriverProfileService(DriverRepositoryPort driverRepositoryPort, Clock clock) {
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort, "driverRepositoryPort");
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public void provisionForDriverUser(UserId userId) {
		Objects.requireNonNull(userId, "userId is required");
		if (driverRepositoryPort.existsByUserId(userId)) {
			throw new DriverAlreadyLinkedToUserException(userId.value());
		}

		OffsetDateTime createdAt = BusinessTime.toBusinessTime(clock.instant());
		Driver driver = Driver.create(DriverId.generate(), userId, null, null, createdAt);
		driverRepositoryPort.save(driver);
	}
}
