package com.fleetbite.driver.infrastructure.inbound.rest.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DriverResponse(
		UUID id,
		String name,
		String phone,
		String status,
		Double currentLatitude,
		Double currentLongitude,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
}
