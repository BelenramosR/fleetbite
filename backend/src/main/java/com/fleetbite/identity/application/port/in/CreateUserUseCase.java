package com.fleetbite.identity.application.port.in;

import com.fleetbite.identity.application.dto.CreateUserCommand;
import com.fleetbite.identity.application.dto.UserResult;

public interface CreateUserUseCase {

	UserResult execute(CreateUserCommand command);
}
