package com.fleetbite.identity.application.port.in;

import com.fleetbite.identity.application.dto.LoginCommand;
import com.fleetbite.identity.application.dto.LoginResult;

public interface LoginUseCase {

	LoginResult execute(LoginCommand command);
}
