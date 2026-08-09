package com.fleetbite.delivery.infrastructure.inbound.rest.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AssignmentResponse(
		UUID id,
		UUID orderId,
		UUID driverId,
		String status,
		OffsetDateTime assignedAt,
		OffsetDateTime acceptedAt,
		OffsetDateTime rejectedAt,
		OffsetDateTime pickedUpAt,
		OffsetDateTime completedAt,
		String rejectionReason,
		BigDecimal assignmentScore,
		OffsetDateTime createdAt) {
}
