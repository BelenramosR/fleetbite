package com.fleetbite.identity.application.port.in;

import com.fleetbite.identity.application.dto.LoginResult;
import com.fleetbite.identity.application.dto.RefreshTokenCommand;

public interface RefreshAccessTokenUseCase {

	LoginResult execute(RefreshTokenCommand command);
}
