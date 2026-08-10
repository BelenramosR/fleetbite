package com.fleetbite.delivery.application.service;

import java.util.UUID;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.RejectAssignmentCommand;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
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

public final class RejectAssignmentService {

	private final DeliveryAssignmentRepositoryPort assignmentRepositoryPort;
	private final OrderRepositoryPort orderRepositoryPort;
	private final DriverRepositoryPort driverRepositoryPort;
	private final OrderHistoryRecorder orderHistoryRecorder;
	private final Clock clock;

	public RejectAssignmentService(
			DeliveryAssignmentRepositoryPort assignmentRepositoryPort,
			OrderRepositoryPort orderRepositoryPort,
			DriverRepositoryPort driverRepositoryPort,
			OrderHistoryRecorder orderHistoryRecorder,
			Clock clock) {
		this.assignmentRepositoryPort = Objects.requireNonNull(assignmentRepositoryPort);
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort);
		this.orderHistoryRecorder = Objects.requireNonNull(orderHistoryRecorder);
		this.clock = Objects.requireNonNull(clock);
	}

	public AssignmentResult execute(UUID assignmentId, RejectAssignmentCommand command) {
		Objects.requireNonNull(assignmentId, "assignmentId is required");
		Objects.requireNonNull(command, "command is required");

		DeliveryAssignment assignment = assignmentRepositoryPort.findById(assignmentId)
				.orElseThrow(() -> new ResourceNotFoundException("DeliveryAssignment", assignmentId));
		Order order = orderRepositoryPort.findById(assignment.orderId())
				.orElseThrow(() -> new ResourceNotFoundException("Order", assignment.orderId()));
		Driver driver = driverRepositoryPort.findById(assignment.driverId())
				.orElseThrow(() -> new ResourceNotFoundException("Driver", assignment.driverId()));

		OrderStatus previous = order.status();
		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		assignment.reject(command.reason(), now);
		order.markWaitingForDriver();
		driver.markAvailable(now);

		DeliveryAssignment updated = assignmentRepositoryPort.update(assignment);
		orderRepositoryPort.update(order);
		driverRepositoryPort.update(driver);
		orderHistoryRecorder.record(
				order.id(),
				OrderHistoryEventType.ASSIGNMENT_REJECTED,
				previous,
				order.status(),
				command.reason(),
				now);

		return AssignmentResult.from(updated);
	}
}
