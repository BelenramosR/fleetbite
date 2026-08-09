package com.fleetbite.vehicle.infrastructure.inbound.rest.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VehicleResponse(
		UUID id,
		String plate,
		String type,
		String status,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
}
