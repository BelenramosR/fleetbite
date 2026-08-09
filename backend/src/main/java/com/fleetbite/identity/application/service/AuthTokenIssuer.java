package com.fleetbite.identity.application.service;

import com.fleetbite.identity.application.dto.LoginResult;
import com.fleetbite.identity.application.port.out.RefreshTokenRepositoryPort;
import com.fleetbite.identity.application.port.out.TokenProviderPort;
import com.fleetbite.identity.application.util.RefreshTokenHasher;
import com.fleetbite.identity.domain.model.RefreshToken;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public final class AuthTokenIssuer {

	private final TokenProviderPort tokenProviderPort;
	private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
	private final long refreshExpirationSeconds;
	private final Clock clock;

	public AuthTokenIssuer(
			TokenProviderPort tokenProviderPort,
			RefreshTokenRepositoryPort refreshTokenRepositoryPort,
			long refreshExpirationSeconds,
			Clock clock) {
		this.tokenProviderPort = Objects.requireNonNull(tokenProviderPort);
		this.refreshTokenRepositoryPort = Objects.requireNonNull(refreshTokenRepositoryPort);
		if (refreshExpirationSeconds <= 0) {
			throw new IllegalArgumentException("refreshExpirationSeconds must be positive");
		}
		this.refreshExpirationSeconds = refreshExpirationSeconds;
		this.clock = Objects.requireNonNull(clock);
	}

	public LoginResult issue(User user) {
		Objects.requireNonNull(user, "user is required");
		String accessToken = tokenProviderPort.generate(user.id(), user.email(), user.role());
		String rawRefreshToken = UUID.randomUUID().toString();
		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		RefreshToken refreshToken = RefreshToken.issue(
				UUID.randomUUID(),
				user.id(),
				RefreshTokenHasher.sha256Hex(rawRefreshToken),
				now,
				now.plusSeconds(refreshExpirationSeconds));
		refreshTokenRepositoryPort.save(refreshToken);
		return LoginResult.bearer(accessToken, tokenProviderPort.expiresInSeconds(), rawRefreshToken);
	}
}
