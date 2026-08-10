package com.fleetbite.delivery.application.port.in;

import java.util.UUID;

import com.fleetbite.delivery.application.dto.AutoAssignmentResult;

public interface AutoAssignOrderUseCase {

	AutoAssignmentResult execute(UUID orderId);
}
