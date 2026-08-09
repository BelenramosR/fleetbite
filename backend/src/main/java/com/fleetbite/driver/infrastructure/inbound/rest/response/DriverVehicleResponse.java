package com.fleetbite.driver.infrastructure.inbound.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Assigned vehicle summary embedded in a driver response")
public record DriverVehicleResponse(
		UUID id,
		@Schema(example = "ABC-123") String plate,
		@Schema(example = "MOTORCYCLE") String type,
		@Schema(allowableValues = {"AVAILABLE", "IN_USE", "MAINTENANCE", "INACTIVE"}, example = "IN_USE")
		String status) {
}
