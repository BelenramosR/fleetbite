package com.fleetbite.identity.application.service;

import com.fleetbite.identity.application.dto.CreateUserCommand;
import com.fleetbite.identity.application.dto.UserResult;
import com.fleetbite.identity.application.port.in.CreateUserUseCase;
import com.fleetbite.identity.application.port.out.DriverProfileProvisionerPort;
import com.fleetbite.identity.application.port.out.PasswordEncoderPort;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.exception.DuplicateUserEmailException;
import com.fleetbite.identity.domain.exception.InvalidUserDataException;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.identity.domain.model.UserRole;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;

public final class CreateUserService implements CreateUserUseCase {

	private final UserRepositoryPort userRepositoryPort;
	private final PasswordEncoderPort passwordEncoderPort;
	private final DriverProfileProvisionerPort driverProfileProvisionerPort;
	private final Clock clock;

	public CreateUserService(
			UserRepositoryPort userRepositoryPort,
			PasswordEncoderPort passwordEncoderPort,
			DriverProfileProvisionerPort driverProfileProvisionerPort,
			Clock clock) {
		this.userRepositoryPort = Objects.requireNonNull(userRepositoryPort);
		this.passwordEncoderPort = Objects.requireNonNull(passwordEncoderPort);
		this.driverProfileProvisionerPort = Objects.requireNonNull(driverProfileProvisionerPort);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override
	public UserResult execute(CreateUserCommand command) {
		Objects.requireNonNull(command, "command is required");
		if (command.role() == null) {
			throw new InvalidUserDataException("role is required");
		}
		if (command.password() == null || command.password().isBlank()) {
			throw new InvalidUserDataException("password is required");
		}

		String email = requireEmail(command.email());
		if (userRepositoryPort.existsByEmail(email)) {
			throw new DuplicateUserEmailException(email);
		}

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		User user = User.create(
				UserId.generate(),
				email,
				passwordEncoderPort.hash(command.password()),
				command.fullName(),
				command.role(),
				now);

		User saved = userRepositoryPort.save(user);
		if (saved.role() == UserRole.DRIVER) {
			driverProfileProvisionerPort.provisionForDriverUser(saved.id());
		}
		return UserResult.from(saved);
	}

	private static String requireEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new InvalidUserDataException("email is required");
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
