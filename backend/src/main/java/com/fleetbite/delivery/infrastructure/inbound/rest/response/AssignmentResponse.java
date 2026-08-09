package com.fleetbite.delivery.infrastructure.inbound.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Delivery assignment resource")
public record AssignmentResponse(
		UUID id,
		UUID orderId,
		UUID driverId,
		@Schema(allowableValues = {"PENDING", "ACCEPTED", "REJECTED", "CANCELLED", "COMPLETED"}, example = "PENDING")
		String status,
		@Schema(example = "2026-08-10T18:40:00-05:00") OffsetDateTime assignedAt,
		OffsetDateTime acceptedAt,
		OffsetDateTime rejectedAt,
		OffsetDateTime pickedUpAt,
		OffsetDateTime completedAt,
		String rejectionReason,
		@Schema(description = "For auto-assign, currently equals distanceKm") BigDecimal assignmentScore,
		@Schema(example = "2026-08-10T18:40:00-05:00") OffsetDateTime createdAt) {
}
