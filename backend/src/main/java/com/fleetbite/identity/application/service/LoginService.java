package com.fleetbite.identity.application.service;

import com.fleetbite.identity.application.dto.LoginCommand;
import com.fleetbite.identity.application.dto.LoginResult;
import com.fleetbite.identity.application.port.in.LoginUseCase;
import com.fleetbite.identity.application.port.out.PasswordEncoderPort;
import com.fleetbite.identity.application.port.out.TokenProviderPort;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.exception.AuthenticationFailedException;
import com.fleetbite.identity.domain.exception.InvalidUserDataException;
import com.fleetbite.identity.domain.model.User;

import java.util.Locale;
import java.util.Objects;

public final class LoginService implements LoginUseCase {

	private final UserRepositoryPort userRepositoryPort;
	private final PasswordEncoderPort passwordEncoderPort;
	private final TokenProviderPort tokenProviderPort;

	public LoginService(
			UserRepositoryPort userRepositoryPort,
			PasswordEncoderPort passwordEncoderPort,
			TokenProviderPort tokenProviderPort) {
		this.userRepositoryPort = Objects.requireNonNull(userRepositoryPort);
		this.passwordEncoderPort = Objects.requireNonNull(passwordEncoderPort);
		this.tokenProviderPort = Objects.requireNonNull(tokenProviderPort);
	}

	@Override
	public LoginResult execute(LoginCommand command) {
		Objects.requireNonNull(command, "command is required");
		if (command.email() == null || command.email().isBlank()) {
			throw new InvalidUserDataException("email is required");
		}
		if (command.password() == null || command.password().isBlank()) {
			throw new InvalidUserDataException("password is required");
		}

		String email = command.email().trim().toLowerCase(Locale.ROOT);
		User user = userRepositoryPort.findByEmail(email)
				.orElseThrow(AuthenticationFailedException::new);

		if (!passwordEncoderPort.matches(command.password(), user.passwordHash())) {
			throw new AuthenticationFailedException();
		}

		user.ensureActive();

		String token = tokenProviderPort.generate(user.id(), user.email(), user.role());
		return LoginResult.bearer(token, tokenProviderPort.expiresInSeconds());
	}
}
