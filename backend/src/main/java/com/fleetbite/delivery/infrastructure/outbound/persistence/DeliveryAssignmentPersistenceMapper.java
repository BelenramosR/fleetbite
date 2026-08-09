package com.fleetbite.delivery.infrastructure.outbound.persistence;

import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.delivery.domain.model.DeliveryAssignmentId;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "orderId", expression = "java(assignment.orderId().value())")
	@Mapping(target = "driverId", expression = "java(assignment.driverId().value())")
	@Mapping(target = "status", expression = "java(assignment.status())")
	@Mapping(target = "assignedAt", expression = "java(assignment.assignedAt())")
	@Mapping(target = "acceptedAt", expression = "java(assignment.acceptedAt())")
	@Mapping(target = "rejectedAt", expression = "java(assignment.rejectedAt())")
	@Mapping(target = "pickedUpAt", expression = "java(assignment.pickedUpAt())")
	@Mapping(target = "completedAt", expression = "java(assignment.completedAt())")
	@Mapping(target = "rejectionReason", expression = "java(assignment.rejectionReason())")
	@Mapping(target = "assignmentScore", expression = "java(assignment.assignmentScore())")
	@Mapping(target = "createdAt", expression = "java(assignment.createdAt())")
	protected abstract void copyPersistableState(
			DeliveryAssignment assignment,
			@MappingTarget DeliveryAssignmentJpaEntity entity);

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
