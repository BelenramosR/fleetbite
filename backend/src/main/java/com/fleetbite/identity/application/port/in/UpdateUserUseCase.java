package com.fleetbite.identity.application.port.in;

import com.fleetbite.identity.application.dto.UpdateUserCommand;
import com.fleetbite.identity.application.dto.UserResult;
import com.fleetbite.identity.domain.model.UserId;

public interface UpdateUserUseCase {

	UserResult execute(UserId userId, UpdateUserCommand command);
}
