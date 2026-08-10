package com.fleetbite.delivery.infrastructure.transaction;

import java.util.UUID;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.port.in.StartDeliveryAssignmentUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalStartDeliveryAssignmentUseCase implements StartDeliveryAssignmentUseCase {

	private final StartDeliveryAssignmentUseCase delegate;

	public TransactionalStartDeliveryAssignmentUseCase(StartDeliveryAssignmentUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public AssignmentResult execute(UUID assignmentId) {
		return delegate.execute(assignmentId);
	}
}
