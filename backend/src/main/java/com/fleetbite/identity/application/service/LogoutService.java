package com.fleetbite.identity.application.service;

import com.fleetbite.identity.application.dto.RefreshTokenCommand;
import com.fleetbite.identity.application.port.in.LogoutUseCase;
import com.fleetbite.identity.application.port.out.RefreshTokenRepositoryPort;
import com.fleetbite.identity.application.util.RefreshTokenHasher;
import com.fleetbite.identity.domain.exception.InvalidUserDataException;
import com.fleetbite.identity.domain.model.RefreshToken;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

public final class LogoutService implements LogoutUseCase {

	private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
	private final Clock clock;

	public LogoutService(RefreshTokenRepositoryPort refreshTokenRepositoryPort, Clock clock) {
		this.refreshTokenRepositoryPort = Objects.requireNonNull(refreshTokenRepositoryPort);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override
	public void execute(RefreshTokenCommand command) {
		Objects.requireNonNull(command, "command is required");
		if (command.refreshToken() == null || command.refreshToken().isBlank()) {
			throw new InvalidUserDataException("refreshToken is required");
		}

		String tokenHash = RefreshTokenHasher.sha256Hex(command.refreshToken().trim());
		Optional<RefreshToken> existing = refreshTokenRepositoryPort.findByHash(tokenHash);
		if (existing.isEmpty()) {
			return;
		}

		RefreshToken token = existing.get();
		if (token.isRevoked()) {
			return;
		}

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		refreshTokenRepositoryPort.revoke(token.id(), now);
	}
}
