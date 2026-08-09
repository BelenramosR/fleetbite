package com.fleetbite.vehicle.infrastructure.inbound.rest.request;

import com.fleetbite.vehicle.domain.model.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Update vehicle")
public record UpdateVehicleRequest(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Size(max = 16) String plate,
		@Schema(implementation = VehicleType.class, requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull VehicleType type) {
}
