package com.fleetbite.delivery.application.service;

import java.util.UUID;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.port.in.AcceptAssignmentUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.order.application.service.OrderHistoryRecorder;
import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class AcceptAssignmentService implements AcceptAssignmentUseCase {

	private final DeliveryAssignmentRepositoryPort assignmentRepositoryPort;
	private final OrderHistoryRecorder orderHistoryRecorder;
	private final Clock clock;

	public AcceptAssignmentService(
			DeliveryAssignmentRepositoryPort assignmentRepositoryPort,
			OrderHistoryRecorder orderHistoryRecorder,
			Clock clock) {
		this.assignmentRepositoryPort = Objects.requireNonNull(assignmentRepositoryPort);
		this.orderHistoryRecorder = Objects.requireNonNull(orderHistoryRecorder);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override
	public AssignmentResult execute(UUID assignmentId) {
		Objects.requireNonNull(assignmentId, "assignmentId is required");

		DeliveryAssignment assignment = assignmentRepositoryPort.findById(assignmentId)
				.orElseThrow(() -> new ResourceNotFoundException("DeliveryAssignment", assignmentId));

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		assignment.accept(now);

		DeliveryAssignment updated = assignmentRepositoryPort.update(assignment);
		orderHistoryRecorder.record(
				assignment.orderId(),
				OrderHistoryEventType.ASSIGNMENT_ACCEPTED,
				OrderStatus.ASSIGNED,
				OrderStatus.ASSIGNED,
				"Assignment accepted",
				now);
		return AssignmentResult.from(updated);
	}
}
