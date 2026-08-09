package com.fleetbite.identity.domain.model;

import com.fleetbite.identity.domain.exception.InvalidUserDataException;

import java.util.Objects;
import java.util.UUID;

public final class UserId {

	private final UUID value;

	private UserId(UUID value) {
		this.value = value;
	}

	public static UserId of(UUID value) {
		if (value == null) {
			throw new InvalidUserDataException("userId is required");
		}
		return new UserId(value);
	}

	public static UserId generate() {
		return new UserId(UUID.randomUUID());
	}

	public UUID value() {
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof UserId that)) {
			return false;
		}
		return value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
