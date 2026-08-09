package com.fleetbite.identity.application.port.in;

import com.fleetbite.identity.application.dto.UserResult;
import com.fleetbite.identity.domain.model.UserId;

public interface ActivateUserUseCase {

	UserResult execute(UserId userId);
}
