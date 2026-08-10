package com.fleetbite.identity.application.service;

import java.util.UUID;

import com.fleetbite.identity.application.dto.UserResult;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;

import java.util.Objects;

public final class GetUserByIdService {

	private final UserRepositoryPort userRepositoryPort;

	public GetUserByIdService(UserRepositoryPort userRepositoryPort) {
		this.userRepositoryPort = Objects.requireNonNull(userRepositoryPort);
	}

	public UserResult execute(UUID userId) {
		Objects.requireNonNull(userId, "userId is required");
		return userRepositoryPort.findById(userId)
				.map(UserResult::from)
				.orElseThrow(() -> new ResourceNotFoundException("User", userId));
	}
}
