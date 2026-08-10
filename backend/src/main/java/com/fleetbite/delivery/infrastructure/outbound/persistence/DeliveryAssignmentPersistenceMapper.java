package com.fleetbite.delivery.infrastructure.outbound.persistence;

import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;

@Component
public class DeliveryAssignmentPersistenceMapper {

	public DeliveryAssignmentJpaEntity toEntity(DeliveryAssignment assignment) {
		Objects.requireNonNull(assignment, "assignment is required");
		DeliveryAssignmentJpaEntity entity = new DeliveryAssignmentJpaEntity();
		entity.setId(assignment.id());
		copyPersistableState(assignment, entity);
		return entity;
	}

	public void copyToEntity(DeliveryAssignment assignment, DeliveryAssignmentJpaEntity existingEntity) {
		Objects.requireNonNull(assignment, "assignment is required");
		Objects.requireNonNull(existingEntity, "existingEntity is required");
		if (!existingEntity.getId().equals(assignment.id())) {
			throw new IllegalArgumentException("cannot copy assignment onto entity with a different id");
		}
		copyPersistableState(assignment, existingEntity);
	}

	private void copyPersistableState(DeliveryAssignment assignment, DeliveryAssignmentJpaEntity entity) {
		entity.setOrderId(assignment.orderId());
		entity.setDriverId(assignment.driverId());
		entity.setStatus(assignment.status());
		entity.setAssignedAt(assignment.assignedAt());
		entity.setAcceptedAt(assignment.acceptedAt());
		entity.setRejectedAt(assignment.rejectedAt());
		entity.setPickedUpAt(assignment.pickedUpAt());
		entity.setCompletedAt(assignment.completedAt());
		entity.setRejectionReason(assignment.rejectionReason());
		entity.setAssignmentScore(assignment.assignmentScore());
		entity.setCreatedAt(assignment.createdAt());
	}

	public DeliveryAssignment toDomain(DeliveryAssignmentJpaEntity entity) {
		Objects.requireNonNull(entity, "entity is required");
		return DeliveryAssignment.reconstitute(
				entity.getId(),
				entity.getOrderId(),
				entity.getDriverId(),
				entity.getStatus(),
				toBusinessOffset(entity.getAssignedAt()),
				toBusinessOffset(entity.getAcceptedAt()),
				toBusinessOffset(entity.getRejectedAt()),
				toBusinessOffset(entity.getPickedUpAt()),
				toBusinessOffset(entity.getCompletedAt()),
				entity.getRejectionReason(),
				entity.getAssignmentScore(),
				toBusinessOffset(entity.getCreatedAt()));
	}

	private OffsetDateTime toBusinessOffset(OffsetDateTime value) {
		return value == null ? null : value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}
}
