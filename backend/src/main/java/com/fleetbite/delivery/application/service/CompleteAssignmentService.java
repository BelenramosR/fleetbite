package com.fleetbite.delivery.application.service;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.port.in.CompleteAssignmentUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.exception.InvalidAssignmentTransitionException;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.delivery.domain.model.DeliveryAssignmentId;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class CompleteAssignmentService implements CompleteAssignmentUseCase {

	private final DeliveryAssignmentRepositoryPort assignmentRepositoryPort;
	private final OrderRepositoryPort orderRepositoryPort;
	private final DriverRepositoryPort driverRepositoryPort;
	private final Clock clock;

	public CompleteAssignmentService(
			DeliveryAssignmentRepositoryPort assignmentRepositoryPort,
			OrderRepositoryPort orderRepositoryPort,
			DriverRepositoryPort driverRepositoryPort,
			Clock clock) {
		this.assignmentRepositoryPort = Objects.requireNonNull(assignmentRepositoryPort);
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override
	public AssignmentResult execute(DeliveryAssignmentId assignmentId) {
		Objects.requireNonNull(assignmentId, "assignmentId is required");

		DeliveryAssignment assignment = assignmentRepositoryPort.findById(assignmentId)
				.orElseThrow(() -> new ResourceNotFoundException("DeliveryAssignment", assignmentId.value()));
		Order order = orderRepositoryPort.findById(assignment.orderId())
				.orElseThrow(() -> new ResourceNotFoundException("Order", assignment.orderId().value()));
		Driver driver = driverRepositoryPort.findById(assignment.driverId())
				.orElseThrow(() -> new ResourceNotFoundException("Driver", assignment.driverId().value()));

		if (order.status() != OrderStatus.IN_TRANSIT) {
			throw new InvalidAssignmentTransitionException(
					"Complete requires order status IN_TRANSIT");
		}

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		assignment.complete(now);
		order.deliver(now);
		driver.markAvailable(now);

		DeliveryAssignment updated = assignmentRepositoryPort.update(assignment);
		orderRepositoryPort.update(order);
		driverRepositoryPort.update(driver);

		return AssignmentResult.from(updated);
	}
}
