package com.fleetbite.delivery.application.service;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.port.in.StartDeliveryAssignmentUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.exception.InvalidAssignmentTransitionException;
import com.fleetbite.delivery.domain.model.AssignmentStatus;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.delivery.domain.model.DeliveryAssignmentId;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class StartDeliveryAssignmentService implements StartDeliveryAssignmentUseCase {

	private final DeliveryAssignmentRepositoryPort assignmentRepositoryPort;
	private final OrderRepositoryPort orderRepositoryPort;
	private final Clock clock;

	public StartDeliveryAssignmentService(
			DeliveryAssignmentRepositoryPort assignmentRepositoryPort,
			OrderRepositoryPort orderRepositoryPort,
			Clock clock) {
		this.assignmentRepositoryPort = Objects.requireNonNull(assignmentRepositoryPort);
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override
	public AssignmentResult execute(DeliveryAssignmentId assignmentId) {
		Objects.requireNonNull(assignmentId, "assignmentId is required");

		DeliveryAssignment assignment = assignmentRepositoryPort.findById(assignmentId)
				.orElseThrow(() -> new ResourceNotFoundException("DeliveryAssignment", assignmentId.value()));
		Order order = orderRepositoryPort.findById(assignment.orderId())
				.orElseThrow(() -> new ResourceNotFoundException("Order", assignment.orderId().value()));

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

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		order.startDelivery(now);
		orderRepositoryPort.update(order);

		return AssignmentResult.from(assignment);
	}
}
