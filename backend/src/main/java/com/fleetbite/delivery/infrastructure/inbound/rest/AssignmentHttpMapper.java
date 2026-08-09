package com.fleetbite.delivery.infrastructure.inbound.rest;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.CreateManualAssignmentCommand;
import com.fleetbite.delivery.application.dto.RejectAssignmentCommand;
import com.fleetbite.delivery.infrastructure.inbound.rest.request.CreateManualAssignmentRequest;
import com.fleetbite.delivery.infrastructure.inbound.rest.request.RejectAssignmentRequest;
import com.fleetbite.delivery.infrastructure.inbound.rest.response.AssignmentResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AssignmentHttpMapper {

	public CreateManualAssignmentCommand toCommand(UUID orderId, CreateManualAssignmentRequest request) {
		return new CreateManualAssignmentCommand(orderId, request.driverId());
	}

	public RejectAssignmentCommand toCommand(RejectAssignmentRequest request) {
		return new RejectAssignmentCommand(request.reason());
	}

	public AssignmentResponse toResponse(AssignmentResult result) {
		return new AssignmentResponse(
				result.id(),
				result.orderId(),
				result.driverId(),
				result.status().name(),
				result.assignedAt(),
				result.acceptedAt(),
				result.rejectedAt(),
				result.pickedUpAt(),
				result.completedAt(),
				result.rejectionReason(),
				result.assignmentScore(),
				result.createdAt());
	}
}
