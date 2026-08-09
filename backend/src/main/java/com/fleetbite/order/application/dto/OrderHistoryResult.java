package com.fleetbite.order.application.dto;

import com.fleetbite.order.domain.model.OrderHistoryEvent;
import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderStatus;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record OrderHistoryResult(
		UUID id,
		OrderHistoryEventType eventType,
		OrderStatus previousStatus,
		OrderStatus newStatus,
		String description,
		OffsetDateTime createdAt) {

	public static OrderHistoryResult from(OrderHistoryEvent event) {
		Objects.requireNonNull(event, "event is required");
		return new OrderHistoryResult(
				event.id().value(),
				event.eventType(),
				event.previousStatus(),
				event.newStatus(),
				event.description(),
				event.createdAt());
	}
}
