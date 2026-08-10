package com.fleetbite.delivery.application.service;

import java.util.UUID;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.port.in.GetAssignmentByIdUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;

import java.util.Objects;

public final class GetAssignmentByIdService implements GetAssignmentByIdUseCase {

	private final DeliveryAssignmentRepositoryPort assignmentRepositoryPort;

	public GetAssignmentByIdService(DeliveryAssignmentRepositoryPort assignmentRepositoryPort) {
		this.assignmentRepositoryPort = Objects.requireNonNull(assignmentRepositoryPort);
	}

	@Override
	public AssignmentResult execute(UUID assignmentId) {
		Objects.requireNonNull(assignmentId, "assignmentId is required");
		return assignmentRepositoryPort.findById(assignmentId)
				.map(AssignmentResult::from)
				.orElseThrow(() -> new ResourceNotFoundException("DeliveryAssignment", assignmentId));
	}
}
