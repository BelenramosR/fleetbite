package com.fleetbite.delivery.domain.model;

public enum AssignmentStatus {
	PENDING,
	ACCEPTED,
	REJECTED,
	CANCELLED,
	COMPLETED;

	public boolean isActive() {
		return this == PENDING || this == ACCEPTED;
	}
}
