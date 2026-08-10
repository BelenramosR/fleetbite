package com.fleetbite.order.domain.event;

import com.fleetbite.shared.domain.event.DomainEvent;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Emitted when an order transitions to READY. Payload is intentionally minimal;
 * consumers load the current Order via application use cases.
 */
public record OrderReadyEvent(
		UUID eventId,
		UUID orderId,
		OffsetDateTime occurredAt) implements DomainEvent {

	public OrderReadyEvent {
		Objects.requireNonNull(eventId, "eventId is required");
		Objects.requireNonNull(orderId, "orderId is required");
		Objects.requireNonNull(occurredAt, "occurredAt is required");
	}

	public static OrderReadyEvent of(UUID orderId, OffsetDateTime occurredAt) {
		return new OrderReadyEvent(UUID.randomUUID(), orderId, occurredAt);
	}
}
