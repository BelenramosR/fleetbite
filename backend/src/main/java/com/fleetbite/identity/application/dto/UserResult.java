package com.fleetbite.identity.application.dto;

import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserRole;
import com.fleetbite.identity.domain.model.UserStatus;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public final class UserResult {

	private final UUID id;
	private final String email;
	private final String fullName;
	private final UserRole role;
	private final UserStatus status;
	private final OffsetDateTime createdAt;
	private final OffsetDateTime updatedAt;

	private UserResult(
			UUID id,
			String email,
			String fullName,
			UserRole role,
			UserStatus status,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt) {
		this.id = id;
		this.email = email;
		this.fullName = fullName;
		this.role = role;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static UserResult from(User user) {
		Objects.requireNonNull(user, "user is required");
		return new UserResult(
				user.id(),
				user.email(),
				user.fullName(),
				user.role(),
				user.status(),
				user.createdAt(),
				user.updatedAt());
	}

	public UUID id() {
		return id;
	}

	public String email() {
		return email;
	}

	public String fullName() {
		return fullName;
	}

	public UserRole role() {
		return role;
	}

	public UserStatus status() {
		return status;
	}

	public OffsetDateTime createdAt() {
		return createdAt;
	}

	public OffsetDateTime updatedAt() {
		return updatedAt;
	}
}
