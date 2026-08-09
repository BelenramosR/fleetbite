package com.fleetbite.order.domain.model;

import com.fleetbite.order.domain.exception.InvalidOrderDataException;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Append-only timeline entry for an order. Never updated or deleted after creation.
 */
public final class OrderHistoryEvent {

	private final OrderHistoryEventId id;
	private final OrderId orderId;
	private final OrderHistoryEventType eventType;
	private final OrderStatus previousStatus;
	private final OrderStatus newStatus;
	private final String description;
	private final UUID performedBy;
	private final OffsetDateTime createdAt;

	private OrderHistoryEvent(
			OrderHistoryEventId id,
			OrderId orderId,
			OrderHistoryEventType eventType,
			OrderStatus previousStatus,
			OrderStatus newStatus,
			String description,
			UUID performedBy,
			OffsetDateTime createdAt) {
		this.id = id;
		this.orderId = orderId;
		this.eventType = eventType;
		this.previousStatus = previousStatus;
		this.newStatus = newStatus;
		this.description = description;
		this.performedBy = performedBy;
		this.createdAt = createdAt;
	}

	public static OrderHistoryEvent record(
			OrderId orderId,
			OrderHistoryEventType eventType,
			OrderStatus previousStatus,
			OrderStatus newStatus,
			String description,
			OffsetDateTime createdAt) {
		return record(orderId, eventType, previousStatus, newStatus, description, null, createdAt);
	}

	public static OrderHistoryEvent record(
			OrderId orderId,
			OrderHistoryEventType eventType,
			OrderStatus previousStatus,
			OrderStatus newStatus,
			String description,
			UUID performedBy,
			OffsetDateTime createdAt) {
		if (orderId == null) {
			throw new InvalidOrderDataException("orderId is required");
		}
		if (eventType == null) {
			throw new InvalidOrderDataException("eventType is required");
		}
		if (newStatus == null) {
			throw new InvalidOrderDataException("newStatus is required");
		}
		if (createdAt == null) {
			throw new InvalidOrderDataException("createdAt is required");
		}
		String normalizedDescription = null;
		if (description != null) {
			normalizedDescription = description.trim();
			if (normalizedDescription.isEmpty()) {
				throw new InvalidOrderDataException("description must not be blank when provided");
			}
			if (normalizedDescription.length() > 500) {
				throw new InvalidOrderDataException("description must be at most 500 characters");
			}
		}
		return new OrderHistoryEvent(
				OrderHistoryEventId.generate(),
				orderId,
				eventType,
				previousStatus,
				newStatus,
				normalizedDescription,
				performedBy,
				createdAt);
	}

	public static OrderHistoryEvent reconstitute(
			OrderHistoryEventId id,
			OrderId orderId,
			OrderHistoryEventType eventType,
			OrderStatus previousStatus,
			OrderStatus newStatus,
			String description,
			UUID performedBy,
			OffsetDateTime createdAt) {
		if (id == null) {
			throw new InvalidOrderDataException("orderHistoryEventId is required");
		}
		if (orderId == null) {
			throw new InvalidOrderDataException("orderId is required");
		}
		if (eventType == null) {
			throw new InvalidOrderDataException("eventType is required");
		}
		if (newStatus == null) {
			throw new InvalidOrderDataException("newStatus is required");
		}
		if (createdAt == null) {
			throw new InvalidOrderDataException("createdAt is required");
		}
		return new OrderHistoryEvent(
				id,
				orderId,
				eventType,
				previousStatus,
				newStatus,
				description,
				performedBy,
				createdAt);
	}

	public OrderHistoryEventId id() {
		return id;
	}

	public OrderId orderId() {
		return orderId;
	}

	public OrderHistoryEventType eventType() {
		return eventType;
	}

	public OrderStatus previousStatus() {
		return previousStatus;
	}

	public OrderStatus newStatus() {
		return newStatus;
	}

	public String description() {
		return description;
	}

	public UUID performedBy() {
		return performedBy;
	}

	public OffsetDateTime createdAt() {
		return createdAt;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof OrderHistoryEvent that)) {
			return false;
		}
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
