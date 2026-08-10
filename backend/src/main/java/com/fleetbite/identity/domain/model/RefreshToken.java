package com.fleetbite.identity.domain.model;

import com.fleetbite.identity.domain.exception.InvalidUserDataException;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public final class RefreshToken {

	private final UUID id;
	private final UUID userId;
	private final String tokenHash;
	private final OffsetDateTime expiresAt;
	private final OffsetDateTime createdAt;
	private OffsetDateTime revokedAt;

	private RefreshToken(
			UUID id,
			UUID userId,
			String tokenHash,
			OffsetDateTime expiresAt,
			OffsetDateTime createdAt,
			OffsetDateTime revokedAt) {
		this.id = id;
		this.userId = userId;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
		this.createdAt = createdAt;
		this.revokedAt = revokedAt;
	}

	public static RefreshToken issue(
			UUID id,
			UUID userId,
			String tokenHash,
			OffsetDateTime createdAt,
			OffsetDateTime expiresAt) {
		if (id == null) {
			throw new InvalidUserDataException("refreshToken id is required");
		}
		if (userId == null) {
			throw new InvalidUserDataException("userId is required");
		}
		if (tokenHash == null || tokenHash.isBlank()) {
			throw new InvalidUserDataException("tokenHash is required");
		}
		if (createdAt == null) {
			throw new InvalidUserDataException("createdAt is required");
		}
		if (expiresAt == null) {
			throw new InvalidUserDataException("expiresAt is required");
		}
		if (!expiresAt.isAfter(createdAt)) {
			throw new InvalidUserDataException("expiresAt must be after createdAt");
		}
		return new RefreshToken(id, userId, tokenHash.trim(), expiresAt, createdAt, null);
	}

	public static RefreshToken reconstitute(
			UUID id,
			UUID userId,
			String tokenHash,
			OffsetDateTime expiresAt,
			OffsetDateTime createdAt,
			OffsetDateTime revokedAt) {
		return new RefreshToken(id, userId, tokenHash, expiresAt, createdAt, revokedAt);
	}

	public void revoke(OffsetDateTime revokedAt) {
		Objects.requireNonNull(revokedAt, "revokedAt is required");
		if (this.revokedAt != null) {
			return;
		}
		this.revokedAt = revokedAt;
	}

	public boolean isRevoked() {
		return revokedAt != null;
	}

	public boolean isExpired(OffsetDateTime now) {
		Objects.requireNonNull(now, "now is required");
		return !expiresAt.isAfter(now);
	}

	public boolean isUsable(OffsetDateTime now) {
		return !isRevoked() && !isExpired(now);
	}

	public UUID id() {
		return id;
	}

	public UUID userId() {
		return userId;
	}

	public String tokenHash() {
		return tokenHash;
	}

	public OffsetDateTime expiresAt() {
		return expiresAt;
	}

	public OffsetDateTime createdAt() {
		return createdAt;
	}

	public OffsetDateTime revokedAt() {
		return revokedAt;
	}
}
