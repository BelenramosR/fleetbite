package com.fleetbite.delivery.application.service;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.DriverActiveAssignmentResult;
import com.fleetbite.delivery.application.dto.DriverAssignmentSummaryResult;
import com.fleetbite.delivery.application.dto.RejectAssignmentCommand;
import com.fleetbite.delivery.application.port.in.AssignmentWorkflowUseCase;
import com.fleetbite.delivery.application.port.in.DriverAssignmentUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.shared.application.exception.ForbiddenOperationException;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;

import java.util.Objects;
import java.util.UUID;
import java.time.Clock;
import com.fleetbite.delivery.domain.model.AssignmentStatus;
import com.fleetbite.shared.domain.time.BusinessTime;

public final class DriverAssignmentService implements DriverAssignmentUseCase {

	private final DriverRepositoryPort drivers;
	private final DeliveryAssignmentRepositoryPort assignments;
	private final OrderRepositoryPort orders;
	private final AssignmentWorkflowUseCase workflow;
	private final Clock clock;

	public DriverAssignmentService(
			DriverRepositoryPort drivers,
			DeliveryAssignmentRepositoryPort assignments,
			OrderRepositoryPort orders,
			AssignmentWorkflowUseCase workflow,
			Clock clock) {
		this.drivers = Objects.requireNonNull(drivers);
		this.assignments = Objects.requireNonNull(assignments);
		this.orders = Objects.requireNonNull(orders);
		this.workflow = Objects.requireNonNull(workflow);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override public DriverAssignmentSummaryResult getSummary(UUID userId) {
		Driver driver = resolveDriver(userId);
		var assignmentsForDriver = assignments.findAllByDriverId(driver.id());
		var today = BusinessTime.toBusinessTime(clock.instant()).toLocalDate();
		long completedToday = assignmentsForDriver.stream()
				.filter(a -> a.status() == AssignmentStatus.COMPLETED)
				.filter(a -> a.completedAt() != null && a.completedAt().toLocalDate().equals(today))
				.count();
		long accepted = assignmentsForDriver.stream()
				.filter(a -> a.status() == AssignmentStatus.ACCEPTED || a.status() == AssignmentStatus.COMPLETED)
				.count();
		long rejected = assignmentsForDriver.stream()
				.filter(a -> a.status() == AssignmentStatus.REJECTED).count();
		long decisions = accepted + rejected;
		int rate = decisions == 0 ? 0 : (int) Math.round(accepted * 100.0 / decisions);
		return new DriverAssignmentSummaryResult(completedToday, accepted, rejected, rate);
	}

	@Override public DriverActiveAssignmentResult getActive(UUID userId) {
		Driver driver = resolveDriver(userId);
		DeliveryAssignment assignment = assignments.findActiveByDriverId(driver.id())
				.orElseThrow(() -> new ResourceNotFoundException("Active assignment for driver", driver.id()));
		return new DriverActiveAssignmentResult(
				AssignmentResult.from(assignment),
				orders.findById(assignment.orderId())
						.map(OrderResult::from)
						.orElseThrow(() -> new ResourceNotFoundException("Order", assignment.orderId())));
	}

	@Override public AssignmentResult accept(UUID userId, UUID assignmentId) {
		verifyOwnership(userId, assignmentId);
		return workflow.accept(assignmentId);
	}

	@Override public AssignmentResult reject(
			UUID userId, UUID assignmentId, RejectAssignmentCommand command) {
		verifyOwnership(userId, assignmentId);
		return workflow.reject(assignmentId, command);
	}

	@Override public AssignmentResult pickup(UUID userId, UUID assignmentId) {
		verifyOwnership(userId, assignmentId);
		return workflow.pickup(assignmentId);
	}

	@Override public AssignmentResult startDelivery(UUID userId, UUID assignmentId) {
		verifyOwnership(userId, assignmentId);
		return workflow.startDelivery(assignmentId);
	}

	@Override public AssignmentResult complete(UUID userId, UUID assignmentId) {
		verifyOwnership(userId, assignmentId);
		return workflow.complete(assignmentId);
	}

	private void verifyOwnership(UUID userId, UUID assignmentId) {
		Driver driver = resolveDriver(userId);
		DeliveryAssignment assignment = assignments.findById(assignmentId)
				.orElseThrow(() -> new ResourceNotFoundException("DeliveryAssignment", assignmentId));
		if (!assignment.driverId().equals(driver.id())) {
			throw new ForbiddenOperationException("Assignment does not belong to the authenticated driver");
		}
	}

	private Driver resolveDriver(UUID userId) {
		Objects.requireNonNull(userId, "userId is required");
		return drivers.findByUserId(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Driver for user", userId));
	}
}
