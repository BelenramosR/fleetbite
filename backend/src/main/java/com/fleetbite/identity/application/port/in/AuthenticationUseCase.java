package com.fleetbite.identity.application.port.in;

import com.fleetbite.identity.application.dto.LoginCommand;
import com.fleetbite.identity.application.dto.LoginResult;
import com.fleetbite.identity.application.dto.RefreshTokenCommand;

public interface AuthenticationUseCase {

	LoginResult login(LoginCommand command);

	LoginResult refresh(RefreshTokenCommand command);

	void logout(RefreshTokenCommand command);
}
