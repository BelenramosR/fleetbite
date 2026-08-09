package com.fleetbite.order.domain.model;

import com.fleetbite.order.domain.exception.InvalidOrderDataException;

import java.util.Objects;
import java.util.UUID;

public final class OrderHistoryEventId {

	private final UUID value;

	private OrderHistoryEventId(UUID value) {
		this.value = value;
	}

	public static OrderHistoryEventId of(UUID value) {
		if (value == null) {
			throw new InvalidOrderDataException("orderHistoryEventId is required");
		}
		return new OrderHistoryEventId(value);
	}

	public static OrderHistoryEventId generate() {
		return new OrderHistoryEventId(UUID.randomUUID());
	}

	public UUID value() {
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof OrderHistoryEventId that)) {
			return false;
		}
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
