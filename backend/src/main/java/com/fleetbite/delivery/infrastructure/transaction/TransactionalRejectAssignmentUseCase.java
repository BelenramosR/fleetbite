package com.fleetbite.delivery.infrastructure.transaction;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.RejectAssignmentCommand;
import com.fleetbite.delivery.application.port.in.RejectAssignmentUseCase;
import com.fleetbite.delivery.domain.model.DeliveryAssignmentId;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalRejectAssignmentUseCase implements RejectAssignmentUseCase {

	private final RejectAssignmentUseCase delegate;

	public TransactionalRejectAssignmentUseCase(RejectAssignmentUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public AssignmentResult execute(DeliveryAssignmentId assignmentId, RejectAssignmentCommand command) {
		return delegate.execute(assignmentId, command);
	}
}
