package com.fleetbite.delivery.application.port.in;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.domain.model.DeliveryAssignmentId;

public interface CompleteAssignmentUseCase {

	AssignmentResult execute(DeliveryAssignmentId assignmentId);
}
