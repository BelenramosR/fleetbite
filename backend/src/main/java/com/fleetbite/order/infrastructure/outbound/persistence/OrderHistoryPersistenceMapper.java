package com.fleetbite.order.infrastructure.outbound.persistence;

import com.fleetbite.order.domain.model.OrderHistoryEvent;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;

@Component
public class OrderHistoryPersistenceMapper {

	public OrderHistoryJpaEntity toEntity(OrderHistoryEvent event) {
		Objects.requireNonNull(event, "event is required");
		OrderHistoryJpaEntity entity = new OrderHistoryJpaEntity();
		entity.setId(event.id());
		copyPersistableState(event, entity);
		return entity;
	}

	private void copyPersistableState(OrderHistoryEvent event, OrderHistoryJpaEntity entity) {
		entity.setOrderId(event.orderId());
		entity.setEventType(event.eventType());
		entity.setPreviousStatus(event.previousStatus());
		entity.setNewStatus(event.newStatus());
		entity.setDescription(event.description());
		entity.setPerformedBy(event.performedBy());
		entity.setCreatedAt(event.createdAt());
	}

	public OrderHistoryEvent toDomain(OrderHistoryJpaEntity entity) {
		Objects.requireNonNull(entity, "entity is required");
		return OrderHistoryEvent.reconstitute(
				entity.getId(),
				entity.getOrderId(),
				entity.getEventType(),
				entity.getPreviousStatus(),
				entity.getNewStatus(),
				entity.getDescription(),
				entity.getPerformedBy(),
				toBusinessOffset(entity.getCreatedAt()));
	}

	private OffsetDateTime toBusinessOffset(OffsetDateTime value) {
		if (value == null) {
			return null;
		}
		return value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}
}
