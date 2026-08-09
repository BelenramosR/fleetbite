package com.fleetbite.identity.application.service;

import com.fleetbite.identity.application.dto.UserResult;
import com.fleetbite.identity.application.port.in.GetUserByIdUseCase;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;

import java.util.Objects;

public final class GetUserByIdService implements GetUserByIdUseCase {

	private final UserRepositoryPort userRepositoryPort;

	public GetUserByIdService(UserRepositoryPort userRepositoryPort) {
		this.userRepositoryPort = Objects.requireNonNull(userRepositoryPort);
	}

	@Override
	public UserResult execute(UserId userId) {
		Objects.requireNonNull(userId, "userId is required");
		return userRepositoryPort.findById(userId)
				.map(UserResult::from)
				.orElseThrow(() -> new ResourceNotFoundException("User", userId.value()));
	}
}
