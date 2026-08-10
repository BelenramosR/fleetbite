package com.fleetbite.order.application.service;

import java.util.UUID;

import com.fleetbite.order.application.dto.CancelOrderCommand;
import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.in.CancelOrderUseCase;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.exception.InvalidOrderDataException;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class CancelOrderService implements CancelOrderUseCase {

	private final OrderRepositoryPort orderRepositoryPort;
	private final OrderHistoryRecorder orderHistoryRecorder;
	private final Clock clock;

	public CancelOrderService(
			OrderRepositoryPort orderRepositoryPort,
			OrderHistoryRecorder orderHistoryRecorder,
			Clock clock) {
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
		this.orderHistoryRecorder = Objects.requireNonNull(orderHistoryRecorder);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override
	public OrderResult execute(UUID orderId, CancelOrderCommand command) {
		Objects.requireNonNull(orderId, "orderId is required");
		Objects.requireNonNull(command, "command is required");

		String reason = command.reason();
		if (reason != null && reason.isBlank()) {
			throw new InvalidOrderDataException("reason must not be blank when provided");
		}

		Order order = orderRepositoryPort.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

		OrderStatus previous = order.status();
		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		order.cancel(now);

		Order updated = orderRepositoryPort.update(order);
		orderHistoryRecorder.record(
				orderId,
				OrderHistoryEventType.ORDER_CANCELLED,
				previous,
				updated.status(),
				reason == null ? null : reason.trim(),
				now);
		return OrderResult.from(updated);
	}
}
