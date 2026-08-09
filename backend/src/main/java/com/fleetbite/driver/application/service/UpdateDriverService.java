package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.dto.UpdateDriverCommand;
import com.fleetbite.driver.application.port.in.UpdateDriverUseCase;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.exception.DuplicateDriverPhoneException;
import com.fleetbite.driver.domain.exception.InvalidDriverDataException;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class UpdateDriverService implements UpdateDriverUseCase {

	private final DriverRepositoryPort driverRepositoryPort;
	private final Clock clock;

	public UpdateDriverService(DriverRepositoryPort driverRepositoryPort, Clock clock) {
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort, "driverRepositoryPort");
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public DriverResult execute(DriverId driverId, UpdateDriverCommand command) {
		Objects.requireNonNull(driverId, "driverId is required");
		Objects.requireNonNull(command, "command is required");

		Driver driver = driverRepositoryPort.findById(driverId)
				.orElseThrow(() -> new ResourceNotFoundException("Driver", driverId.value()));

		String phone = requireTrimmed(command.phone(), "phone");
		if (driverRepositoryPort.existsByPhoneAndIdNot(phone, driverId)) {
			throw new DuplicateDriverPhoneException(phone);
		}

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		driver.updateProfile(command.name(), phone, now);

		Driver updated = driverRepositoryPort.update(driver);
		return DriverResult.from(updated);
	}

	private static String requireTrimmed(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new InvalidDriverDataException(fieldName + " is required");
		}
		return value.trim();
	}
}
