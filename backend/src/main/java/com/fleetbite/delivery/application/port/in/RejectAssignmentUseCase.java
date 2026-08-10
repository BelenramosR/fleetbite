package com.fleetbite.delivery.application.port.in;

import java.util.UUID;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.RejectAssignmentCommand;

public interface RejectAssignmentUseCase {

	AssignmentResult execute(UUID assignmentId, RejectAssignmentCommand command);
}
