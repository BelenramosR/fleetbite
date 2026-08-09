package com.fleetbite.delivery.infrastructure.outbound.persistence;

import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.delivery.domain.model.DeliveryAssignmentId;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.mapstruct.Mapper;

import java.time.OffsetDateTime;
import java.util.Objects;

@Mapper(componentModel = "spring")
public abstract class DeliveryAssignmentPersistenceMapper {

	public DeliveryAssignmentJpaEntity toEntity(DeliveryAssignment assignment) {
		Objects.requireNonNull(assignment, "assignment is required");

		DeliveryAssignmentJpaEntity entity = new DeliveryAssignmentJpaEntity();
		entity.setId(assignment.id().value());
		copyPersistableState(assignment, entity);
		return entity;
	}

	public void copyToEntity(DeliveryAssignment assignment, DeliveryAssignmentJpaEntity existingEntity) {
		Objects.requireNonNull(assignment, "assignment is required");
		Objects.requireNonNull(existingEntity, "existingEntity is required");
		if (!existingEntity.getId().equals(assignment.id().value())) {
			throw new IllegalArgumentException("cannot copy assignment onto entity with a different id");
		}
		copyPersistableState(assignment, existingEntity);
	}

	protected void copyPersistableState(DeliveryAssignment assignment, DeliveryAssignmentJpaEntity entity) {
		entity.setOrderId(assignment.orderId().value());
		entity.setDriverId(assignment.driverId().value());
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
				DeliveryAssignmentId.of(entity.getId()),
				OrderId.of(entity.getOrderId()),
				DriverId.of(entity.getDriverId()),
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

	protected OffsetDateTime toBusinessOffset(OffsetDateTime value) {
		if (value == null) {
			return null;
		}
		return value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}
}
