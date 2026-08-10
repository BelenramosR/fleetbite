package com.fleetbite.delivery.application.service;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.port.in.AssignmentQueryUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AssignmentQueryService implements AssignmentQueryUseCase {

	private final DeliveryAssignmentRepositoryPort repositoryPort;

	public AssignmentQueryService(DeliveryAssignmentRepositoryPort repositoryPort) {
		this.repositoryPort = Objects.requireNonNull(repositoryPort);
	}

	@Override
	public AssignmentResult getById(UUID assignmentId) {
		Objects.requireNonNull(assignmentId, "assignmentId is required");
		return repositoryPort.findById(assignmentId)
				.map(AssignmentResult::from)
				.orElseThrow(() -> new ResourceNotFoundException("DeliveryAssignment", assignmentId));
	}

	@Override
	public List<AssignmentResult> findAll() {
		return repositoryPort.findAll().stream().map(AssignmentResult::from).toList();
	}
}
