package com.fleetbite.delivery.application.service;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.RejectAssignmentCommand;
import com.fleetbite.delivery.application.port.in.AssignmentWorkflowUseCase;
import com.fleetbite.delivery.application.port.in.DriverAssignmentUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.shared.application.exception.ForbiddenOperationException;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;

import java.util.Objects;
import java.util.UUID;

public final class DriverAssignmentService implements DriverAssignmentUseCase {

	private final DriverRepositoryPort drivers;
	private final DeliveryAssignmentRepositoryPort assignments;
	private final AssignmentWorkflowUseCase workflow;

	public DriverAssignmentService(
			DriverRepositoryPort drivers,
			DeliveryAssignmentRepositoryPort assignments,
			AssignmentWorkflowUseCase workflow) {
		this.drivers = Objects.requireNonNull(drivers);
		this.assignments = Objects.requireNonNull(assignments);
		this.workflow = Objects.requireNonNull(workflow);
	}

	@Override public AssignmentResult getActive(UUID userId) {
		Driver driver = resolveDriver(userId);
		return assignments.findActiveByDriverId(driver.id())
				.map(AssignmentResult::from)
				.orElseThrow(() -> new ResourceNotFoundException("Active assignment for driver", driver.id()));
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
