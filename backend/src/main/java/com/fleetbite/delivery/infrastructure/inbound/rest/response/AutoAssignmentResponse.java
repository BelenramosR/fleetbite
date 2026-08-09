package com.fleetbite.delivery.infrastructure.inbound.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Auto-assignment result. Always HTTP 200 when the command is valid; check assigned.")
public record AutoAssignmentResponse(
		@Schema(description = "true when a driver was assigned") boolean assigned,
		UUID orderId,
		@Schema(description = "Present when assigned=true") UUID assignmentId,
		@Schema(description = "Present when assigned=true") UUID driverId,
		@Schema(description = "Haversine distance in km when assigned") BigDecimal distanceKm,
		@Schema(description = "Currently equals distanceKm when assigned") BigDecimal score,
		@Schema(example = "ASSIGNED") String orderStatus,
		@Schema(description = "NO_AVAILABLE_DRIVER when assigned=false", example = "NO_AVAILABLE_DRIVER")
		String reason) {
}
