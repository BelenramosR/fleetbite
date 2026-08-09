package com.fleetbite.delivery.application.port.in;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.CreateManualAssignmentCommand;

public interface CreateManualAssignmentUseCase {

	AssignmentResult execute(CreateManualAssignmentCommand command);
}
