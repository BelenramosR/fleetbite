package com.fleetbite.driver.infrastructure.inbound.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Driver resource")
public record DriverResponse(
		UUID id,
		String name,
		String phone,
		@Schema(allowableValues = {"OFFLINE", "AVAILABLE", "BUSY"}, example = "AVAILABLE")
		String status,
		Double currentLatitude,
		Double currentLongitude,
		@Schema(example = "2026-08-10T18:20:00-05:00") OffsetDateTime createdAt,
		@Schema(example = "2026-08-10T18:20:00-05:00") OffsetDateTime updatedAt) {
}
