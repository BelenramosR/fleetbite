package com.fleetbite.delivery.infrastructure.transaction;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.CreateManualAssignmentCommand;
import com.fleetbite.delivery.application.port.in.CreateManualAssignmentUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalCreateManualAssignmentUseCase implements CreateManualAssignmentUseCase {

	private final CreateManualAssignmentUseCase delegate;

	public TransactionalCreateManualAssignmentUseCase(CreateManualAssignmentUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public AssignmentResult execute(CreateManualAssignmentCommand command) {
		return delegate.execute(command);
	}
}
