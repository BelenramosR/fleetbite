package com.fleetbite.delivery.infrastructure.transaction;

import java.util.UUID;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.RejectAssignmentCommand;
import com.fleetbite.delivery.application.port.in.RejectAssignmentUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalRejectAssignmentUseCase implements RejectAssignmentUseCase {

	private final RejectAssignmentUseCase delegate;

	public TransactionalRejectAssignmentUseCase(RejectAssignmentUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public AssignmentResult execute(UUID assignmentId, RejectAssignmentCommand command) {
		return delegate.execute(assignmentId, command);
	}
}
