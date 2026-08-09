package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.CreateDriverCommand;
import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.in.CreateDriverUseCase;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.exception.DuplicateDriverPhoneException;
import com.fleetbite.driver.domain.exception.InvalidDriverDataException;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class CreateDriverService implements CreateDriverUseCase {

	private final DriverRepositoryPort driverRepositoryPort;
	private final Clock clock;

	public CreateDriverService(DriverRepositoryPort driverRepositoryPort, Clock clock) {
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort, "driverRepositoryPort");
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public DriverResult execute(CreateDriverCommand command) {
		Objects.requireNonNull(command, "command is required");

		String phone = requireTrimmed(command.phone(), "phone");
		if (driverRepositoryPort.existsByPhone(phone)) {
			throw new DuplicateDriverPhoneException(phone);
		}

		OffsetDateTime createdAt = BusinessTime.toBusinessTime(clock.instant());
		Driver driver = Driver.create(
				DriverId.generate(),
				command.name(),
				phone,
				resolveLocation(command.currentLatitude(), command.currentLongitude()),
				createdAt);

		Driver saved = driverRepositoryPort.save(driver);
		return DriverResult.from(saved);
	}

	private static Location resolveLocation(Double latitude, Double longitude) {
		if (latitude == null && longitude == null) {
			return null;
		}
		if (latitude == null || longitude == null) {
			throw new InvalidDriverDataException(
					"currentLatitude and currentLongitude must both be provided or both omitted");
		}
		try {
			return new Location(latitude, longitude);
		}
		catch (IllegalArgumentException exception) {
			throw new InvalidDriverDataException(exception.getMessage());
		}
	}

	private static String requireTrimmed(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new InvalidDriverDataException(fieldName + " is required");
		}
		return value.trim();
	}
}
