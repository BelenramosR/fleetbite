package com.fleetbite.delivery.domain.model;

import com.fleetbite.delivery.domain.exception.InvalidAssignmentDataException;

import java.util.Objects;
import java.util.UUID;

public final class DeliveryAssignmentId {

	private final UUID value;

	private DeliveryAssignmentId(UUID value) {
		this.value = value;
	}

	public static DeliveryAssignmentId of(UUID value) {
		if (value == null) {
			throw new InvalidAssignmentDataException("assignmentId is required");
		}
		return new DeliveryAssignmentId(value);
	}

	public static DeliveryAssignmentId generate() {
		return new DeliveryAssignmentId(UUID.randomUUID());
	}

	public UUID value() {
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof DeliveryAssignmentId that)) {
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
