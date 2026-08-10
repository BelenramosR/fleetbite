package com.fleetbite.delivery.application.port.in;

import java.util.UUID;

import com.fleetbite.delivery.application.dto.AssignmentResult;

public interface CompleteAssignmentUseCase {

	AssignmentResult execute(UUID assignmentId);
}
