package com.fleetbite.vehicle.application.dto;

import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleStatus;
import com.fleetbite.vehicle.domain.model.VehicleType;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record VehicleResult(
		UUID id,
		String plate,
		VehicleType type,
		VehicleStatus status,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {

	public static VehicleResult from(Vehicle vehicle) {
		Objects.requireNonNull(vehicle, "vehicle is required");
		return new VehicleResult(
				vehicle.id(), vehicle.plate(), vehicle.type(), vehicle.status(),
				vehicle.createdAt(), vehicle.updatedAt());
	}
}
