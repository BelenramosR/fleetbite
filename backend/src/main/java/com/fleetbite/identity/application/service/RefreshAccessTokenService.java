package com.fleetbite.identity.application.service;

import com.fleetbite.identity.application.dto.LoginResult;
import com.fleetbite.identity.application.dto.RefreshTokenCommand;
import com.fleetbite.identity.application.port.out.RefreshTokenRepositoryPort;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.application.util.RefreshTokenHasher;
import com.fleetbite.identity.domain.exception.AuthenticationFailedException;
import com.fleetbite.identity.domain.exception.InvalidUserDataException;
import com.fleetbite.identity.domain.model.RefreshToken;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class RefreshAccessTokenService {

	private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
	private final UserRepositoryPort userRepositoryPort;
	private final AuthTokenIssuer authTokenIssuer;
	private final Clock clock;

	public RefreshAccessTokenService(
			RefreshTokenRepositoryPort refreshTokenRepositoryPort,
			UserRepositoryPort userRepositoryPort,
			AuthTokenIssuer authTokenIssuer,
			Clock clock) {
		this.refreshTokenRepositoryPort = Objects.requireNonNull(refreshTokenRepositoryPort);
		this.userRepositoryPort = Objects.requireNonNull(userRepositoryPort);
		this.authTokenIssuer = Objects.requireNonNull(authTokenIssuer);
		this.clock = Objects.requireNonNull(clock);
	}

	public LoginResult execute(RefreshTokenCommand command) {
		Objects.requireNonNull(command, "command is required");
		if (command.refreshToken() == null || command.refreshToken().isBlank()) {
			throw new InvalidUserDataException("refreshToken is required");
		}

		String tokenHash = RefreshTokenHasher.sha256Hex(command.refreshToken().trim());
		RefreshToken existing = refreshTokenRepositoryPort.findByHash(tokenHash)
				.orElseThrow(AuthenticationFailedException::new);

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		if (!existing.isUsable(now)) {
			throw new AuthenticationFailedException();
		}

		User user = userRepositoryPort.findById(existing.userId())
				.orElseThrow(AuthenticationFailedException::new);
		user.ensureActive();

		refreshTokenRepositoryPort.revoke(existing.id(), now);
		return authTokenIssuer.issue(user);
	}
}
