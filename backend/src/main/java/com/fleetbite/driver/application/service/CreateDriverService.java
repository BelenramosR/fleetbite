package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.CreateDriverCommand;
import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.in.CreateDriverUseCase;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.exception.DriverAlreadyLinkedToUserException;
import com.fleetbite.driver.domain.exception.DriverUserNotEligibleException;
import com.fleetbite.driver.domain.exception.DuplicateDriverPhoneException;
import com.fleetbite.driver.domain.exception.InvalidDriverDataException;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.identity.domain.model.UserRole;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class CreateDriverService implements CreateDriverUseCase {

	private final UserRepositoryPort userRepositoryPort;
	private final DriverRepositoryPort driverRepositoryPort;
	private final Clock clock;

	public CreateDriverService(
			UserRepositoryPort userRepositoryPort,
			DriverRepositoryPort driverRepositoryPort,
			Clock clock) {
		this.userRepositoryPort = Objects.requireNonNull(userRepositoryPort, "userRepositoryPort");
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort, "driverRepositoryPort");
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public DriverResult execute(CreateDriverCommand command) {
		Objects.requireNonNull(command, "command is required");
		if (command.userId() == null) {
			throw new InvalidDriverDataException("userId is required");
		}

		UserId userId = UserId.of(command.userId());
		User user = userRepositoryPort.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", command.userId()));

		if (user.role() != UserRole.DRIVER) {
			throw new DriverUserNotEligibleException(command.userId());
		}
		if (driverRepositoryPort.existsByUserId(userId)) {
			throw new DriverAlreadyLinkedToUserException(command.userId());
		}

		String phone = requireTrimmed(command.phone(), "phone");
		if (driverRepositoryPort.existsByPhone(phone)) {
			throw new DuplicateDriverPhoneException(phone);
		}

		OffsetDateTime createdAt = BusinessTime.toBusinessTime(clock.instant());
		Driver driver = Driver.create(
				DriverId.generate(),
				userId,
				phone,
				resolveLocation(command.currentLatitude(), command.currentLongitude()),
				createdAt);

		Driver saved = driverRepositoryPort.save(driver);
		return DriverResult.from(saved, user.fullName());
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
