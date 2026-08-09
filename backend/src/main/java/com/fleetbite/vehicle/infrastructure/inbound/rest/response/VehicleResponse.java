package com.fleetbite.vehicle.infrastructure.inbound.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Vehicle resource. Status lifecycle: AVAILABLE, IN_USE, MAINTENANCE, INACTIVE.")
public record VehicleResponse(
		UUID id,
		@Schema(example = "ABC-123") String plate,
		@Schema(example = "MOTORCYCLE") String type,
		@Schema(allowableValues = {"AVAILABLE", "IN_USE", "MAINTENANCE", "INACTIVE"}, example = "AVAILABLE")
		String status,
		@Schema(example = "2026-08-10T18:20:00-05:00") OffsetDateTime createdAt,
		@Schema(example = "2026-08-10T18:20:00-05:00") OffsetDateTime updatedAt) {
}
