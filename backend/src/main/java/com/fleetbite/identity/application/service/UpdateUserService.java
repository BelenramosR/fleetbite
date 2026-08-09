package com.fleetbite.identity.application.service;

import com.fleetbite.identity.application.dto.UpdateUserCommand;
import com.fleetbite.identity.application.dto.UserResult;
import com.fleetbite.identity.application.port.in.UpdateUserUseCase;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.exception.InvalidUserDataException;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class UpdateUserService implements UpdateUserUseCase {

	private final UserRepositoryPort userRepositoryPort;
	private final Clock clock;

	public UpdateUserService(UserRepositoryPort userRepositoryPort, Clock clock) {
		this.userRepositoryPort = Objects.requireNonNull(userRepositoryPort);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override
	public UserResult execute(UserId userId, UpdateUserCommand command) {
		Objects.requireNonNull(userId, "userId is required");
		Objects.requireNonNull(command, "command is required");
		if (command.role() == null) {
			throw new InvalidUserDataException("role is required");
		}

		User user = userRepositoryPort.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", userId.value()));

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		user.updateProfile(command.fullName(), command.role(), now);

		User updated = userRepositoryPort.update(user);
		return UserResult.from(updated);
	}
}
