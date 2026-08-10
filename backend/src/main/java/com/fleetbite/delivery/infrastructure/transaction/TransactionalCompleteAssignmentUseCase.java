package com.fleetbite.delivery.infrastructure.transaction;

import java.util.UUID;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.port.in.CompleteAssignmentUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalCompleteAssignmentUseCase implements CompleteAssignmentUseCase {

	private final CompleteAssignmentUseCase delegate;

	public TransactionalCompleteAssignmentUseCase(CompleteAssignmentUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public AssignmentResult execute(UUID assignmentId) {
		return delegate.execute(assignmentId);
	}
}
