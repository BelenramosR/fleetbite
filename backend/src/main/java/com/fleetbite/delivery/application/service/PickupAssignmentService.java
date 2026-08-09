package com.fleetbite.delivery.application.service;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.port.in.PickupAssignmentUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.delivery.domain.model.DeliveryAssignmentId;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class PickupAssignmentService implements PickupAssignmentUseCase {

	private final DeliveryAssignmentRepositoryPort assignmentRepositoryPort;
	private final OrderRepositoryPort orderRepositoryPort;
	private final Clock clock;

	public PickupAssignmentService(
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

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		assignment.markPickedUp(now);
		order.pickUp(now);

		DeliveryAssignment updated = assignmentRepositoryPort.update(assignment);
		orderRepositoryPort.update(order);

		return AssignmentResult.from(updated);
	}
}
