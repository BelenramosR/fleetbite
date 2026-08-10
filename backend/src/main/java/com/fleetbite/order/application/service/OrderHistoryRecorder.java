package com.fleetbite.order.application.service;

import java.util.UUID;

import com.fleetbite.order.application.port.out.OrderHistoryRepositoryPort;
import com.fleetbite.order.domain.model.OrderHistoryEvent;
import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderStatus;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Shared helper to append OrderHistoryEvent entries without Spring coupling.
 */
public final class OrderHistoryRecorder {

	private final OrderHistoryRepositoryPort orderHistoryRepositoryPort;

	public OrderHistoryRecorder(OrderHistoryRepositoryPort orderHistoryRepositoryPort) {
		this.orderHistoryRepositoryPort = Objects.requireNonNull(orderHistoryRepositoryPort);
	}

	public void record(
			UUID orderId,
			OrderHistoryEventType eventType,
			OrderStatus previousStatus,
			OrderStatus newStatus,
			String description,
			OffsetDateTime createdAt) {
		orderHistoryRepositoryPort.save(OrderHistoryEvent.record(
				orderId,
				eventType,
				previousStatus,
				newStatus,
				description,
				createdAt));
	}
}
