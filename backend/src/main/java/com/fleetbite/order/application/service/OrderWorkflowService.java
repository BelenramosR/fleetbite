package com.fleetbite.order.application.service;

import com.fleetbite.order.application.dto.CancelOrderCommand;
import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.in.OrderWorkflowUseCase;
import com.fleetbite.order.application.port.out.DomainEventPublisherPort;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.event.OrderReadyEvent;
import com.fleetbite.order.domain.exception.InvalidOrderDataException;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public final class OrderWorkflowService implements OrderWorkflowUseCase {

	private final OrderRepositoryPort orderRepositoryPort;
	private final OrderHistoryRecorder orderHistoryRecorder;
	private final DomainEventPublisherPort domainEventPublisherPort;
	private final Clock clock;

	public OrderWorkflowService(
			OrderRepositoryPort orderRepositoryPort,
			OrderHistoryRecorder orderHistoryRecorder,
			DomainEventPublisherPort domainEventPublisherPort,
			Clock clock) {
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
		this.orderHistoryRecorder = Objects.requireNonNull(orderHistoryRecorder);
		this.domainEventPublisherPort = Objects.requireNonNull(domainEventPublisherPort);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override
	public OrderResult confirm(UUID orderId) {
		Order order = load(orderId);
		OrderStatus previous = order.status();
		OffsetDateTime now = now();
		order.confirm(now);
		return updateAndRecord(order, OrderHistoryEventType.ORDER_CONFIRMED, previous, null, now);
	}

	@Override
	public OrderResult startPreparation(UUID orderId) {
		Order order = load(orderId);
		OrderStatus previous = order.status();
		OffsetDateTime now = now();
		order.startPreparation(now);
		return updateAndRecord(order, OrderHistoryEventType.ORDER_PREPARING, previous, null, now);
	}

	@Override
	public OrderResult markReady(UUID orderId) {
		Order order = load(orderId);
		OrderStatus previous = order.status();
		OffsetDateTime now = now();
		order.markReady(now);
		OrderResult result = updateAndRecord(order, OrderHistoryEventType.ORDER_READY, previous, null, now);
		domainEventPublisherPort.publish(OrderReadyEvent.of(orderId, now));
		return result;
	}

	@Override
	public OrderResult cancel(UUID orderId, CancelOrderCommand command) {
		Objects.requireNonNull(command, "command is required");
		String reason = command.reason();
		if (reason != null && reason.isBlank()) {
			throw new InvalidOrderDataException("reason must not be blank when provided");
		}

		Order order = load(orderId);
		OrderStatus previous = order.status();
		OffsetDateTime now = now();
		order.cancel(now);
		return updateAndRecord(
				order,
				OrderHistoryEventType.ORDER_CANCELLED,
				previous,
				reason == null ? null : reason.trim(),
				now);
	}

	private Order load(UUID orderId) {
		Objects.requireNonNull(orderId, "orderId is required");
		return orderRepositoryPort.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
	}

	private OrderResult updateAndRecord(
			Order order,
			OrderHistoryEventType eventType,
			OrderStatus previousStatus,
			String description,
			OffsetDateTime occurredAt) {
		Order updated = orderRepositoryPort.update(order);
		orderHistoryRecorder.record(
				updated.id(),
				eventType,
				previousStatus,
				updated.status(),
				description,
				occurredAt);
		return OrderResult.from(updated);
	}

	private OffsetDateTime now() {
		return BusinessTime.toBusinessTime(clock.instant());
	}
}
