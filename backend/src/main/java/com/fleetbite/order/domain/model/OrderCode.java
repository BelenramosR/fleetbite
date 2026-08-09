package com.fleetbite.order.domain.model;

import com.fleetbite.order.domain.exception.InvalidOrderDataException;

import java.util.Objects;

public final class OrderCode {

	private static final int MAX_LENGTH = 40;

	private final String value;

	private OrderCode(String value) {
		this.value = value;
	}

	public static OrderCode of(String value) {
		if (value == null || value.isBlank()) {
			throw new InvalidOrderDataException("orderCode is required");
		}
		String normalized = value.trim();
		if (normalized.length() > MAX_LENGTH) {
			throw new InvalidOrderDataException("orderCode must not exceed " + MAX_LENGTH + " characters");
		}
		return new OrderCode(normalized);
	}

	public String value() {
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof OrderCode that)) {
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
		return value;
	}
}
