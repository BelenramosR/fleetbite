package com.fleetbite.order.application.service;

import java.util.UUID;

import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.in.StartOrderPreparationUseCase;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class StartOrderPreparationService implements StartOrderPreparationUseCase {

	private final OrderRepositoryPort orderRepositoryPort;
	private final OrderHistoryRecorder orderHistoryRecorder;
	private final Clock clock;

	public StartOrderPreparationService(
			OrderRepositoryPort orderRepositoryPort,
			OrderHistoryRecorder orderHistoryRecorder,
			Clock clock) {
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
		this.orderHistoryRecorder = Objects.requireNonNull(orderHistoryRecorder);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override
	public OrderResult execute(UUID orderId) {
		Objects.requireNonNull(orderId, "orderId is required");
		Order order = orderRepositoryPort.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

		OrderStatus previous = order.status();
		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		order.startPreparation(now);

		Order updated = orderRepositoryPort.update(order);
		orderHistoryRecorder.record(
				orderId,
				OrderHistoryEventType.ORDER_PREPARING,
				previous,
				updated.status(),
				null,
				now);
		return OrderResult.from(updated);
	}
}
