package com.fleetbite.delivery.application.port.in;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.RejectAssignmentCommand;

import java.util.UUID;

public interface AssignmentWorkflowUseCase {

	AssignmentResult accept(UUID assignmentId);

	AssignmentResult reject(UUID assignmentId, RejectAssignmentCommand command);

	AssignmentResult pickup(UUID assignmentId);

	AssignmentResult startDelivery(UUID assignmentId);

	AssignmentResult complete(UUID assignmentId);
}
