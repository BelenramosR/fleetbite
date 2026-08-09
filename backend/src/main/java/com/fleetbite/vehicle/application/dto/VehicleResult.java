package com.fleetbite.vehicle.application.dto;

import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleStatus;
import com.fleetbite.vehicle.domain.model.VehicleType;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public final class VehicleResult {

	private final UUID id;
	private final String plate;
	private final VehicleType type;
	private final VehicleStatus status;
	private final OffsetDateTime createdAt;
	private final OffsetDateTime updatedAt;

	private VehicleResult(
			UUID id,
			String plate,
			VehicleType type,
			VehicleStatus status,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt) {
		this.id = id;
		this.plate = plate;
		this.type = type;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static VehicleResult from(Vehicle vehicle) {
		Objects.requireNonNull(vehicle, "vehicle is required");
		return new VehicleResult(
				vehicle.id().value(),
				vehicle.plate(),
				vehicle.type(),
				vehicle.status(),
				vehicle.createdAt(),
				vehicle.updatedAt());
	}

	public UUID id() {
		return id;
	}

	public String plate() {
		return plate;
	}

	public VehicleType type() {
		return type;
	}

	public VehicleStatus status() {
		return status;
	}

	public OffsetDateTime createdAt() {
		return createdAt;
	}

	public OffsetDateTime updatedAt() {
		return updatedAt;
	}
}
