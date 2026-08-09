package com.fleetbite.identity.application.port.in;

import com.fleetbite.identity.application.dto.RefreshTokenCommand;

public interface LogoutUseCase {

	void execute(RefreshTokenCommand command);
}
