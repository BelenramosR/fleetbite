package com.fleetbite.delivery.application.service;

import java.util.UUID;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.port.in.StartDeliveryAssignmentUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.exception.InvalidAssignmentTransitionException;
import com.fleetbite.delivery.domain.model.AssignmentStatus;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.application.service.OrderHistoryRecorder;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class StartDeliveryAssignmentService implements StartDeliveryAssignmentUseCase {

	private final DeliveryAssignmentRepositoryPort assignmentRepositoryPort;
	private final OrderRepositoryPort orderRepositoryPort;
	private final OrderHistoryRecorder orderHistoryRecorder;
	private final Clock clock;

	public StartDeliveryAssignmentService(
			DeliveryAssignmentRepositoryPort assignmentRepositoryPort,
			OrderRepositoryPort orderRepositoryPort,
			OrderHistoryRecorder orderHistoryRecorder,
			Clock clock) {
		this.assignmentRepositoryPort = Objects.requireNonNull(assignmentRepositoryPort);
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
		this.orderHistoryRecorder = Objects.requireNonNull(orderHistoryRecorder);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override
	public AssignmentResult execute(UUID assignmentId) {
		Objects.requireNonNull(assignmentId, "assignmentId is required");

		DeliveryAssignment assignment = assignmentRepositoryPort.findById(assignmentId)
				.orElseThrow(() -> new ResourceNotFoundException("DeliveryAssignment", assignmentId));
		Order order = orderRepositoryPort.findById(assignment.orderId())
				.orElseThrow(() -> new ResourceNotFoundException("Order", assignment.orderId()));

		if (assignment.status() != AssignmentStatus.ACCEPTED) {
			throw new InvalidAssignmentTransitionException(
					"Start delivery requires assignment status ACCEPTED");
		}
		if (assignment.pickedUpAt() == null) {
			throw new InvalidAssignmentTransitionException(
					"Start delivery requires pickup to be completed first");
		}
		if (order.status() != OrderStatus.PICKED_UP) {
			throw new InvalidAssignmentTransitionException(
					"Start delivery requires order status PICKED_UP");
		}

		OrderStatus previous = order.status();
		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		order.startDelivery(now);
		orderRepositoryPort.update(order);
		orderHistoryRecorder.record(
				order.id(),
				OrderHistoryEventType.ORDER_IN_TRANSIT,
				previous,
				order.status(),
				null,
				now);

		return AssignmentResult.from(assignment);
	}
}
