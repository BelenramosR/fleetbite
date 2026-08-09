package com.fleetbite.driver.domain.model;

import com.fleetbite.driver.domain.exception.InvalidDriverDataException;

import java.util.Objects;
import java.util.UUID;

public final class DriverId {

	private final UUID value;

	private DriverId(UUID value) {
		this.value = value;
	}

	public static DriverId of(UUID value) {
		if (value == null) {
			throw new InvalidDriverDataException("driverId is required");
		}
		return new DriverId(value);
	}

	public static DriverId generate() {
		return new DriverId(UUID.randomUUID());
	}

	public UUID value() {
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof DriverId that)) {
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
