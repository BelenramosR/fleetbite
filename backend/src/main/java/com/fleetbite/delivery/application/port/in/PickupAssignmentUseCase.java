package com.fleetbite.delivery.application.port.in;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.domain.model.DeliveryAssignmentId;

public interface PickupAssignmentUseCase {

	AssignmentResult execute(DeliveryAssignmentId assignmentId);
}
