package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.dto.UpdateDriverLocationCommand;
import com.fleetbite.driver.application.port.in.DriverSelfUseCase;
import com.fleetbite.driver.application.port.in.DriverAvailabilityUseCase;
import com.fleetbite.driver.application.port.in.DriverQueryUseCase;
import com.fleetbite.driver.application.port.in.UpdateDriverLocationUseCase;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;

import java.util.Objects;
import java.util.UUID;

public final class DriverSelfService implements DriverSelfUseCase {

	private final DriverRepositoryPort drivers;
	private final DriverQueryUseCase queries;
	private final UpdateDriverLocationUseCase locationOperation;
	private final DriverAvailabilityUseCase availability;

	public DriverSelfService(
			DriverRepositoryPort drivers,
			DriverQueryUseCase queries,
			UpdateDriverLocationUseCase locationOperation,
			DriverAvailabilityUseCase availability) {
		this.drivers = Objects.requireNonNull(drivers);
		this.queries = Objects.requireNonNull(queries);
		this.locationOperation = Objects.requireNonNull(locationOperation);
		this.availability = Objects.requireNonNull(availability);
	}

	@Override public DriverResult getProfile(UUID userId) {
		return queries.getById(resolve(userId).id());
	}

	@Override public DriverResult updateLocation(UUID userId, UpdateDriverLocationCommand command) {
		return locationOperation.execute(resolve(userId).id(), command);
	}

	@Override public DriverResult goOnline(UUID userId) {
		return availability.goOnline(resolve(userId).id());
	}

	@Override public DriverResult goOffline(UUID userId) {
		return availability.goOffline(resolve(userId).id());
	}

	private Driver resolve(UUID userId) {
		Objects.requireNonNull(userId, "userId is required");
		return drivers.findByUserId(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Driver for user", userId));
	}
}
