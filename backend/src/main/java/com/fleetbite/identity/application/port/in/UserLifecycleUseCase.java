package com.fleetbite.identity.application.port.in;

import com.fleetbite.identity.application.dto.UserResult;

import java.util.UUID;

public interface UserLifecycleUseCase {

	UserResult activate(UUID userId);

	UserResult deactivate(UUID userId);
}
