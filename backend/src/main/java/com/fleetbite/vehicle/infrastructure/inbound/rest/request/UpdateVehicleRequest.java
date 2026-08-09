package com.fleetbite.vehicle.infrastructure.inbound.rest.request;

import com.fleetbite.vehicle.domain.model.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateVehicleRequest(
		@NotBlank @Size(max = 16) String plate,
		@NotNull VehicleType type) {
}
