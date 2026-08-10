package com.fleetbite.driver.application.dto;

import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleStatus;
import com.fleetbite.vehicle.domain.model.VehicleType;

import java.util.Objects;
import java.util.UUID;

public record VehicleSummary(
		UUID id,
		String plate,
		VehicleType type,
		VehicleStatus status) {

	public static VehicleSummary from(Vehicle vehicle) {
		Objects.requireNonNull(vehicle, "vehicle is required");
		return new VehicleSummary(
				vehicle.id(),
				vehicle.plate(),
				vehicle.type(),
				vehicle.status());
	}
}
