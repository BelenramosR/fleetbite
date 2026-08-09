package com.fleetbite.order.infrastructure.outbound.persistence;

import com.fleetbite.order.domain.model.OrderHistoryEvent;
import com.fleetbite.order.domain.model.OrderHistoryEventId;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.OffsetDateTime;
import java.util.Objects;

@Mapper(componentModel = "spring")
public abstract class OrderHistoryPersistenceMapper {

	public OrderHistoryJpaEntity toEntity(OrderHistoryEvent event) {
		Objects.requireNonNull(event, "event is required");
		OrderHistoryJpaEntity entity = new OrderHistoryJpaEntity();
		entity.setId(event.id().value());
		copyPersistableState(event, entity);
		return entity;
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "orderId", expression = "java(event.orderId().value())")
	@Mapping(target = "eventType", expression = "java(event.eventType())")
	@Mapping(target = "previousStatus", expression = "java(event.previousStatus())")
	@Mapping(target = "newStatus", expression = "java(event.newStatus())")
	@Mapping(target = "description", expression = "java(event.description())")
	@Mapping(target = "performedBy", expression = "java(event.performedBy())")
	@Mapping(target = "createdAt", expression = "java(event.createdAt())")
	protected abstract void copyPersistableState(OrderHistoryEvent event, @MappingTarget OrderHistoryJpaEntity entity);

	public OrderHistoryEvent toDomain(OrderHistoryJpaEntity entity) {
		Objects.requireNonNull(entity, "entity is required");
		return OrderHistoryEvent.reconstitute(
				OrderHistoryEventId.of(entity.getId()),
				OrderId.of(entity.getOrderId()),
				entity.getEventType(),
				entity.getPreviousStatus(),
				entity.getNewStatus(),
				entity.getDescription(),
				entity.getPerformedBy(),
				toBusinessOffset(entity.getCreatedAt()));
	}

	protected OffsetDateTime toBusinessOffset(OffsetDateTime value) {
		if (value == null) {
			return null;
		}
		return value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}
}
