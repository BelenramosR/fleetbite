package com.fleetbite.delivery.infrastructure.transaction;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.port.in.AcceptAssignmentUseCase;
import com.fleetbite.delivery.domain.model.DeliveryAssignmentId;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalAcceptAssignmentUseCase implements AcceptAssignmentUseCase {

	private final AcceptAssignmentUseCase delegate;

	public TransactionalAcceptAssignmentUseCase(AcceptAssignmentUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public AssignmentResult execute(DeliveryAssignmentId assignmentId) {
		return delegate.execute(assignmentId);
	}
}
