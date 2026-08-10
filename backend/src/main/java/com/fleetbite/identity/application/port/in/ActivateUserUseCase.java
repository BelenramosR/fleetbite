package com.fleetbite.identity.application.port.in;

import java.util.UUID;

import com.fleetbite.identity.application.dto.UserResult;

public interface ActivateUserUseCase {

	UserResult execute(UUID userId);
}
