package com.fleetbite.vehicle.domain.model;

import com.fleetbite.vehicle.domain.exception.InvalidVehicleDataException;

import java.util.Objects;
import java.util.UUID;

public final class VehicleId {

	private final UUID value;

	private VehicleId(UUID value) {
		this.value = value;
	}

	public static VehicleId of(UUID value) {
		if (value == null) {
			throw new InvalidVehicleDataException("vehicleId is required");
		}
		return new VehicleId(value);
	}

	public static VehicleId generate() {
		return new VehicleId(UUID.randomUUID());
	}

	public UUID value() {
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof VehicleId that)) {
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
