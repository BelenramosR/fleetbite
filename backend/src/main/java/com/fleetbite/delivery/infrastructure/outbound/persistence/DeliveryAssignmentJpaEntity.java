package com.fleetbite.delivery.infrastructure.outbound.persistence;

import com.fleetbite.delivery.domain.model.AssignmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_assignments")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
}
