package com.fleetbite.delivery.application.service;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.port.in.ListAssignmentsUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;

import java.util.List;
import java.util.Objects;

public final class ListAssignmentsService implements ListAssignmentsUseCase {

	private final DeliveryAssignmentRepositoryPort assignmentRepositoryPort;

	public ListAssignmentsService(DeliveryAssignmentRepositoryPort assignmentRepositoryPort) {
		this.assignmentRepositoryPort = Objects.requireNonNull(assignmentRepositoryPort);
	}

	@Override
	public List<AssignmentResult> execute() {
		return assignmentRepositoryPort.findAll().stream()
				.map(AssignmentResult::from)
				.toList();
	}
}
