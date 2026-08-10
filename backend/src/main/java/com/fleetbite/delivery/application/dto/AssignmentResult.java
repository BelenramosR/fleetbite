package com.fleetbite.delivery.application.dto;

import com.fleetbite.delivery.domain.model.AssignmentStatus;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record AssignmentResult(
		UUID id,
		UUID orderId,
		UUID driverId,
		AssignmentStatus status,
		OffsetDateTime assignedAt,
		OffsetDateTime acceptedAt,
		OffsetDateTime rejectedAt,
		OffsetDateTime pickedUpAt,
		OffsetDateTime completedAt,
		String rejectionReason,
		BigDecimal assignmentScore,
		OffsetDateTime createdAt) {

	public static AssignmentResult from(DeliveryAssignment assignment) {
		Objects.requireNonNull(assignment, "assignment is required");
		return new AssignmentResult(
				assignment.id(),
				assignment.orderId(),
				assignment.driverId(),
				assignment.status(),
				assignment.assignedAt(),
				assignment.acceptedAt(),
				assignment.rejectedAt(),
				assignment.pickedUpAt(),
				assignment.completedAt(),
				assignment.rejectionReason(),
				assignment.assignmentScore(),
				assignment.createdAt());
	}
}
