package com.fleetbite.delivery.infrastructure.outbound.persistence;

import com.fleetbite.delivery.domain.model.AssignmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_assignments")
public class DeliveryAssignmentJpaEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "order_id", nullable = false)
	private UUID orderId;

	@Column(name = "driver_id", nullable = false)
	private UUID driverId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 32)
	private AssignmentStatus status;

	@Column(name = "assigned_at", nullable = false)
	private OffsetDateTime assignedAt;

	@Column(name = "accepted_at")
	private OffsetDateTime acceptedAt;

	@Column(name = "rejected_at")
	private OffsetDateTime rejectedAt;

	@Column(name = "picked_up_at")
	private OffsetDateTime pickedUpAt;

	@Column(name = "completed_at")
	private OffsetDateTime completedAt;

	@Column(name = "rejection_reason", length = 255)
	private String rejectionReason;

	@Column(name = "assignment_score", precision = 12, scale = 4)
	private BigDecimal assignmentScore;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	protected DeliveryAssignmentJpaEntity() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getOrderId() {
		return orderId;
	}

	public void setOrderId(UUID orderId) {
		this.orderId = orderId;
	}

	public UUID getDriverId() {
		return driverId;
	}

	public void setDriverId(UUID driverId) {
		this.driverId = driverId;
	}

	public AssignmentStatus getStatus() {
		return status;
	}

	public void setStatus(AssignmentStatus status) {
		this.status = status;
	}

	public OffsetDateTime getAssignedAt() {
		return assignedAt;
	}

	public void setAssignedAt(OffsetDateTime assignedAt) {
		this.assignedAt = assignedAt;
	}

	public OffsetDateTime getAcceptedAt() {
		return acceptedAt;
	}

	public void setAcceptedAt(OffsetDateTime acceptedAt) {
		this.acceptedAt = acceptedAt;
	}

	public OffsetDateTime getRejectedAt() {
		return rejectedAt;
	}

	public void setRejectedAt(OffsetDateTime rejectedAt) {
		this.rejectedAt = rejectedAt;
	}

	public OffsetDateTime getPickedUpAt() {
		return pickedUpAt;
	}

	public void setPickedUpAt(OffsetDateTime pickedUpAt) {
		this.pickedUpAt = pickedUpAt;
	}

	public OffsetDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(OffsetDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public BigDecimal getAssignmentScore() {
		return assignmentScore;
	}

	public void setAssignmentScore(BigDecimal assignmentScore) {
		this.assignmentScore = assignmentScore;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
}
