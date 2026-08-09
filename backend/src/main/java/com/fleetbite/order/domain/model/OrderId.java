package com.fleetbite.order.domain.model;

import com.fleetbite.order.domain.exception.InvalidOrderDataException;

import java.util.Objects;
import java.util.UUID;

public final class OrderId {

	private final UUID value;

	private OrderId(UUID value) {
		this.value = value;
	}

	public static OrderId of(UUID value) {
		if (value == null) {
			throw new InvalidOrderDataException("orderId is required");
		}
		return new OrderId(value);
	}

	public static OrderId generate() {
		return new OrderId(UUID.randomUUID());
	}

	public UUID value() {
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof OrderId that)) {
			return false;
		}
		return value.equals(that.value);
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
