package com.fleetbite.identity.domain.model;

import com.fleetbite.identity.domain.exception.InvalidUserDataException;
import com.fleetbite.identity.domain.exception.UserInactiveException;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class User {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

	private final UserId id;
	private final String email;
	private String passwordHash;
	private String fullName;
	private UserRole role;
	private UserStatus status;
	private final OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;

	private User(
			UserId id,
			String email,
			String passwordHash,
			String fullName,
			UserRole role,
			UserStatus status,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt) {
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.fullName = fullName;
		this.role = role;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static User create(
			UserId id,
			String email,
			String passwordHash,
			String fullName,
			UserRole role,
			OffsetDateTime createdAt) {
		if (id == null) {
			throw new InvalidUserDataException("userId is required");
		}
		if (role == null) {
			throw new InvalidUserDataException("role is required");
		}
		if (createdAt == null) {
			throw new InvalidUserDataException("createdAt is required");
		}
		return new User(
				id,
				normalizeEmail(email),
				requireHash(passwordHash),
				requireText(fullName, "fullName"),
				role,
				UserStatus.ACTIVE,
				createdAt,
				createdAt);
	}

	public static User reconstitute(
			UserId id,
			String email,
			String passwordHash,
			String fullName,
			UserRole role,
			UserStatus status,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt) {
		if (id == null) {
			throw new InvalidUserDataException("userId is required");
		}
		if (role == null) {
			throw new InvalidUserDataException("role is required");
		}
		if (status == null) {
			throw new InvalidUserDataException("status is required");
		}
		if (createdAt == null) {
			throw new InvalidUserDataException("createdAt is required");
		}
		if (updatedAt == null) {
			throw new InvalidUserDataException("updatedAt is required");
		}
		return new User(
				id,
				normalizeEmail(email),
				requireHash(passwordHash),
				requireText(fullName, "fullName"),
				role,
				status,
				createdAt,
				updatedAt);
	}

	public void updateProfile(String fullName, UserRole role, OffsetDateTime now) {
		requireTimestamp(now);
		if (role == null) {
			throw new InvalidUserDataException("role is required");
		}
		this.fullName = requireText(fullName, "fullName");
		this.role = role;
		this.updatedAt = now;
	}

	public void activate(OffsetDateTime now) {
		requireTimestamp(now);
		this.status = UserStatus.ACTIVE;
		this.updatedAt = now;
	}

	public void deactivate(OffsetDateTime now) {
		requireTimestamp(now);
		this.status = UserStatus.INACTIVE;
		this.updatedAt = now;
	}

	public void ensureActive() {
		if (status != UserStatus.ACTIVE) {
			throw new UserInactiveException();
		}
	}

	private static String normalizeEmail(String email) {
		String normalized = requireText(email, "email").toLowerCase(Locale.ROOT);
		if (!EMAIL_PATTERN.matcher(normalized).matches()) {
			throw new InvalidUserDataException("email format is invalid");
		}
		return normalized;
	}

	private static String requireHash(String passwordHash) {
		if (passwordHash == null || passwordHash.isBlank()) {
			throw new InvalidUserDataException("passwordHash is required");
		}
		return passwordHash;
	}

	private static void requireTimestamp(OffsetDateTime now) {
		if (now == null) {
			throw new InvalidUserDataException("timestamp is required");
		}
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new InvalidUserDataException(fieldName + " is required");
		}
		return value.trim();
	}

	public UserId id() {
		return id;
	}

	public String email() {
		return email;
	}

	public String passwordHash() {
		return passwordHash;
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

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof User that)) {
			return false;
		}
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
