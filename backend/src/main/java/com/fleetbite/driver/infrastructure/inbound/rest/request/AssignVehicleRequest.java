package com.fleetbite.driver.infrastructure.inbound.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Assign a vehicle to a driver")
public record AssignVehicleRequest(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull UUID vehicleId) {
}
