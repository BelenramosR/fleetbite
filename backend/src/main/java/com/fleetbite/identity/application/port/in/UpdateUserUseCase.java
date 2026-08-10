package com.fleetbite.identity.application.port.in;

import java.util.UUID;

import com.fleetbite.identity.application.dto.UpdateUserCommand;
import com.fleetbite.identity.application.dto.UserResult;

public interface UpdateUserUseCase {

	UserResult execute(UUID userId, UpdateUserCommand command);
}
