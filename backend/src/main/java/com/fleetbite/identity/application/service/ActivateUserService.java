package com.fleetbite.identity.application.service;

import com.fleetbite.identity.application.dto.UserResult;
import com.fleetbite.identity.application.port.in.ActivateUserUseCase;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class ActivateUserService implements ActivateUserUseCase {

	private final UserRepositoryPort userRepositoryPort;
	private final Clock clock;

	public ActivateUserService(UserRepositoryPort userRepositoryPort, Clock clock) {
		this.userRepositoryPort = Objects.requireNonNull(userRepositoryPort);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override
	public UserResult execute(UserId userId) {
		Objects.requireNonNull(userId, "userId is required");

		User user = userRepositoryPort.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", userId.value()));

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		user.activate(now);

		User updated = userRepositoryPort.update(user);
		return UserResult.from(updated);
	}
}
