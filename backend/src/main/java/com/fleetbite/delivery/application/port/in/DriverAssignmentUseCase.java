package com.fleetbite.delivery.application.port.in;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.RejectAssignmentCommand;

import java.util.UUID;

public interface DriverAssignmentUseCase {

	AssignmentResult getActive(UUID userId);
	AssignmentResult accept(UUID userId, UUID assignmentId);
	AssignmentResult reject(UUID userId, UUID assignmentId, RejectAssignmentCommand command);
	AssignmentResult pickup(UUID userId, UUID assignmentId);
	AssignmentResult startDelivery(UUID userId, UUID assignmentId);
	AssignmentResult complete(UUID userId, UUID assignmentId);
}
