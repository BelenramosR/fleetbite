package com.fleetbite.identity.application.service;

import com.fleetbite.identity.application.dto.UserResult;
import com.fleetbite.identity.application.port.in.UserLifecycleUseCase;

import java.util.Objects;
import java.util.UUID;

public final class UserLifecycleService implements UserLifecycleUseCase {

	private final ActivateUserService activateUserService;
	private final DeactivateUserService deactivateUserService;

	public UserLifecycleService(ActivateUserService activateUserService,
			DeactivateUserService deactivateUserService) {
		this.activateUserService = Objects.requireNonNull(activateUserService);
		this.deactivateUserService = Objects.requireNonNull(deactivateUserService);
	}

	@Override
	public UserResult activate(UUID userId) {
		return activateUserService.execute(userId);
	}

	@Override
	public UserResult deactivate(UUID userId) {
		return deactivateUserService.execute(userId);
	}
}
