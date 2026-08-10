package com.fleetbite.delivery.domain.model;

import java.util.UUID;

import com.fleetbite.delivery.domain.exception.InvalidAssignmentDataException;
import com.fleetbite.delivery.domain.exception.InvalidAssignmentTransitionException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class DeliveryAssignment {

	private final UUID id;
	private final UUID orderId;
	private final UUID driverId;
	private AssignmentStatus status;
	private final OffsetDateTime assignedAt;
	private OffsetDateTime acceptedAt;
	private OffsetDateTime rejectedAt;
	private OffsetDateTime pickedUpAt;
	private OffsetDateTime completedAt;
	private String rejectionReason;
	private final BigDecimal assignmentScore;
	private final OffsetDateTime createdAt;

	private DeliveryAssignment(
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

	public static DeliveryAssignment create(
			UUID id,
			UUID orderId,
			UUID driverId,
			OffsetDateTime assignedAt) {
		return create(id, orderId, driverId, assignedAt, null);
	}

	/**
	 * Creates a PENDING assignment.
	 *
	 * @param assignmentScore optional score; for auto-assign MVP this stores Haversine distanceKm.
	 *                        Manual assign passes {@code null}. A later phase may replace this with a
	 *                        weighted score (workload + SLA).
	 */
	public static DeliveryAssignment create(
			UUID id,
			UUID orderId,
			UUID driverId,
			OffsetDateTime assignedAt,
			BigDecimal assignmentScore) {
		if (id == null) {
			throw new InvalidAssignmentDataException("assignmentId is required");
		}
		if (orderId == null) {
			throw new InvalidAssignmentDataException("orderId is required");
		}
		if (driverId == null) {
			throw new InvalidAssignmentDataException("driverId is required");
		}
		if (assignedAt == null) {
			throw new InvalidAssignmentDataException("assignedAt is required");
		}
		return new DeliveryAssignment(
				id,
				orderId,
				driverId,
				AssignmentStatus.PENDING,
				assignedAt,
				null,
				null,
				null,
				null,
				null,
				assignmentScore,
				assignedAt);
	}

	public static DeliveryAssignment reconstitute(
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
		if (id == null) {
			throw new InvalidAssignmentDataException("assignmentId is required");
		}
		if (orderId == null) {
			throw new InvalidAssignmentDataException("orderId is required");
		}
		if (driverId == null) {
			throw new InvalidAssignmentDataException("driverId is required");
		}
		if (status == null) {
			throw new InvalidAssignmentDataException("status is required");
		}
		if (assignedAt == null) {
			throw new InvalidAssignmentDataException("assignedAt is required");
		}
		if (createdAt == null) {
			throw new InvalidAssignmentDataException("createdAt is required");
		}
		return new DeliveryAssignment(
				id,
				orderId,
				driverId,
				status,
				assignedAt,
				acceptedAt,
				rejectedAt,
				pickedUpAt,
				completedAt,
				rejectionReason,
				assignmentScore,
				createdAt);
	}

	public void accept(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(AssignmentStatus.ACCEPTED);
		this.acceptedAt = now;
	}

	public void reject(String reason, OffsetDateTime now) {
		requireTimestamp(now);
		String trimmedReason = requireText(reason, "rejectionReason");
		transitionTo(AssignmentStatus.REJECTED);
		this.rejectionReason = trimmedReason;
		this.rejectedAt = now;
	}

	public void markPickedUp(OffsetDateTime now) {
		requireTimestamp(now);
		if (status != AssignmentStatus.ACCEPTED) {
			throw new InvalidAssignmentTransitionException(
					"Pickup is only allowed when assignment status is ACCEPTED");
		}
		if (pickedUpAt != null) {
			throw new InvalidAssignmentTransitionException("Assignment already marked as picked up");
		}
		this.pickedUpAt = now;
	}

	public void complete(OffsetDateTime now) {
		requireTimestamp(now);
		if (pickedUpAt == null) {
			throw new InvalidAssignmentTransitionException(
					"Assignment cannot be completed before pickup");
		}
		transitionTo(AssignmentStatus.COMPLETED);
		this.completedAt = now;
	}

	private void transitionTo(AssignmentStatus target) {
		if (!canTransition(status, target)) {
			throw new InvalidAssignmentTransitionException(status, target);
		}
		this.status = target;
	}

	private static boolean canTransition(AssignmentStatus from, AssignmentStatus to) {
		return switch (from) {
			case PENDING -> to == AssignmentStatus.ACCEPTED || to == AssignmentStatus.REJECTED;
			case ACCEPTED -> to == AssignmentStatus.COMPLETED;
			case REJECTED, CANCELLED, COMPLETED -> false;
		};
	}

	private static void requireTimestamp(OffsetDateTime now) {
		if (now == null) {
			throw new InvalidAssignmentDataException("timestamp is required");
		}
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new InvalidAssignmentDataException(fieldName + " is required");
		}
		return value.trim();
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

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof DeliveryAssignment that)) {
			return false;
		}
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
