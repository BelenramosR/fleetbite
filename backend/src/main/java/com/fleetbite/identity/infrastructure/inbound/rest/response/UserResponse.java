package com.fleetbite.identity.infrastructure.inbound.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "User profile. passwordHash is never exposed.")
public record UserResponse(
		UUID id,
		@Schema(example = "admin@fleetbite.local") String email,
		@Schema(example = "FleetBite Admin") String fullName,
		@Schema(allowableValues = {"ADMIN", "DISPATCHER", "RESTAURANT_OPERATOR", "DRIVER"}, example = "ADMIN")
		String role,
		@Schema(allowableValues = {"ACTIVE", "INACTIVE"}, example = "ACTIVE")
		String status,
		@Schema(example = "2026-08-08T22:00:00-05:00") OffsetDateTime createdAt,
		@Schema(example = "2026-08-08T22:00:00-05:00") OffsetDateTime updatedAt) {
}
