package com.fleetbite.order.domain.model;

public enum OrderStatus {
	CREATED,
	CONFIRMED,
	PREPARING,
	READY,
	WAITING_FOR_DRIVER,
	ASSIGNED,
	PICKED_UP,
	IN_TRANSIT,
	DELIVERED,
	CANCELLED,
	FAILED_DELIVERY;

	public boolean isTerminal() {
		return this == DELIVERED || this == CANCELLED || this == FAILED_DELIVERY;
	}
}
