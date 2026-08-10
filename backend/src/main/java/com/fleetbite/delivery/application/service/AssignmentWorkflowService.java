package com.fleetbite.delivery.application.service;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.RejectAssignmentCommand;
import com.fleetbite.delivery.application.port.in.AssignmentWorkflowUseCase;

import java.util.Objects;
import java.util.UUID;

public final class AssignmentWorkflowService implements AssignmentWorkflowUseCase {

	private final AcceptAssignmentService acceptOperation;
	private final RejectAssignmentService rejectOperation;
	private final PickupAssignmentService pickupOperation;
	private final StartDeliveryAssignmentService startDeliveryOperation;
	private final CompleteAssignmentService completeOperation;

	public AssignmentWorkflowService(
			AcceptAssignmentService acceptOperation,
			RejectAssignmentService rejectOperation,
			PickupAssignmentService pickupOperation,
			StartDeliveryAssignmentService startDeliveryOperation,
			CompleteAssignmentService completeOperation) {
		this.acceptOperation = Objects.requireNonNull(acceptOperation);
		this.rejectOperation = Objects.requireNonNull(rejectOperation);
		this.pickupOperation = Objects.requireNonNull(pickupOperation);
		this.startDeliveryOperation = Objects.requireNonNull(startDeliveryOperation);
		this.completeOperation = Objects.requireNonNull(completeOperation);
	}

	@Override public AssignmentResult accept(UUID id) { return acceptOperation.execute(id); }
	@Override public AssignmentResult reject(UUID id, RejectAssignmentCommand command) { return rejectOperation.execute(id, command); }
	@Override public AssignmentResult pickup(UUID id) { return pickupOperation.execute(id); }
	@Override public AssignmentResult startDelivery(UUID id) { return startDeliveryOperation.execute(id); }
	@Override public AssignmentResult complete(UUID id) { return completeOperation.execute(id); }
}
