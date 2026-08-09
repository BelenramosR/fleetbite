package com.fleetbite.order.infrastructure.outbound.persistence;

import com.fleetbite.order.domain.model.OrderHistoryEvent;
import com.fleetbite.order.domain.model.OrderHistoryEventId;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;

@Component
public class OrderHistoryPersistenceMapper {

	public OrderHistoryJpaEntity toEntity(OrderHistoryEvent event) {
		Objects.requireNonNull(event, "event is required");
		OrderHistoryJpaEntity entity = new OrderHistoryJpaEntity();
		entity.setId(event.id().value());
		entity.setOrderId(event.orderId().value());
		entity.setEventType(event.eventType());
		entity.setPreviousStatus(event.previousStatus());
		entity.setNewStatus(event.newStatus());
		entity.setDescription(event.description());
		entity.setPerformedBy(event.performedBy());
		entity.setCreatedAt(event.createdAt());
		return entity;
	}

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

	private static OffsetDateTime toBusinessOffset(OffsetDateTime value) {
		if (value == null) {
			return null;
		}
		return value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}
}
