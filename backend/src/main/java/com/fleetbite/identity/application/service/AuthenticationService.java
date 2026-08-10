package com.fleetbite.identity.application.service;

import com.fleetbite.identity.application.dto.LoginCommand;
import com.fleetbite.identity.application.dto.LoginResult;
import com.fleetbite.identity.application.dto.RefreshTokenCommand;
import com.fleetbite.identity.application.port.in.AuthenticationUseCase;

import java.util.Objects;

public final class AuthenticationService implements AuthenticationUseCase {

	private final LoginService loginService;
	private final RefreshAccessTokenService refreshService;
	private final LogoutService logoutService;

	public AuthenticationService(LoginService loginService, RefreshAccessTokenService refreshService,
			LogoutService logoutService) {
		this.loginService = Objects.requireNonNull(loginService);
		this.refreshService = Objects.requireNonNull(refreshService);
		this.logoutService = Objects.requireNonNull(logoutService);
	}

	@Override
	public LoginResult login(LoginCommand command) {
		return loginService.execute(command);
	}

	@Override
	public LoginResult refresh(RefreshTokenCommand command) {
		return refreshService.execute(command);
	}

	@Override
	public void logout(RefreshTokenCommand command) {
		logoutService.execute(command);
	}
}
