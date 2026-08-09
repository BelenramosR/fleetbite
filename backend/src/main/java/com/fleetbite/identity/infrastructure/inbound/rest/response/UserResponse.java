package com.fleetbite.identity.infrastructure.inbound.rest.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
		UUID id,
		String email,
		String fullName,
		String role,
		String status,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
}
