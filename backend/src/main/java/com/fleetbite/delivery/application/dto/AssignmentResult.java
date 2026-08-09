package com.fleetbite.delivery.application.dto;

import com.fleetbite.delivery.domain.model.AssignmentStatus;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public final class AssignmentResult {

	private final UUID id;
	private final UUID orderId;
	private final UUID driverId;
	private final AssignmentStatus status;
	private final OffsetDateTime assignedAt;
	private final OffsetDateTime acceptedAt;
	private final OffsetDateTime rejectedAt;
	private final OffsetDateTime pickedUpAt;
	private final OffsetDateTime completedAt;
	private final String rejectionReason;
	private final BigDecimal assignmentScore;
	private final OffsetDateTime createdAt;

	private AssignmentResult(
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
		this.id = id;
		this.orderId = orderId;
		this.driverId = driverId;
		this.status = status;
		this.assignedAt = assignedAt;
		this.acceptedAt = acceptedAt;
		this.rejectedAt = rejectedAt;
		this.pickedUpAt = pickedUpAt;
		this.completedAt = completedAt;
		this.rejectionReason = rejectionReason;
		this.assignmentScore = assignmentScore;
		this.createdAt = createdAt;
	}

	public static AssignmentResult from(DeliveryAssignment assignment) {
		Objects.requireNonNull(assignment, "assignment is required");
		return new AssignmentResult(
				assignment.id().value(),
				assignment.orderId().value(),
				assignment.driverId().value(),
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

	public UUID id() {
		return id;
	}

	public UUID orderId() {
		return orderId;
	}

	public UUID driverId() {
		return driverId;
	}

	public AssignmentStatus status() {
		return status;
	}

	public OffsetDateTime assignedAt() {
		return assignedAt;
	}

	public OffsetDateTime acceptedAt() {
		return acceptedAt;
	}

	public OffsetDateTime rejectedAt() {
		return rejectedAt;
	}

	public OffsetDateTime pickedUpAt() {
		return pickedUpAt;
	}

	public OffsetDateTime completedAt() {
		return completedAt;
	}

	public String rejectionReason() {
		return rejectionReason;
	}

	public BigDecimal assignmentScore() {
		return assignmentScore;
	}

	public OffsetDateTime createdAt() {
		return createdAt;
	}
}
