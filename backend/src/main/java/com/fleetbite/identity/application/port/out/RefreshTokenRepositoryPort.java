package com.fleetbite.identity.application.port.out;

import com.fleetbite.identity.domain.model.RefreshToken;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {

	RefreshToken save(RefreshToken refreshToken);

	Optional<RefreshToken> findByHash(String tokenHash);

	void revoke(UUID id, OffsetDateTime revokedAt);

	int deleteExpired(OffsetDateTime now);
}
