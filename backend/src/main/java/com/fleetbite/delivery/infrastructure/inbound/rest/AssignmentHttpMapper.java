package com.fleetbite.delivery.infrastructure.inbound.rest;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.AutoAssignmentResult;
import com.fleetbite.delivery.application.dto.CreateManualAssignmentCommand;
import com.fleetbite.delivery.application.dto.RejectAssignmentCommand;
import com.fleetbite.delivery.infrastructure.inbound.rest.request.CreateManualAssignmentRequest;
import com.fleetbite.delivery.infrastructure.inbound.rest.request.RejectAssignmentRequest;
import com.fleetbite.delivery.infrastructure.inbound.rest.response.AssignmentResponse;
import com.fleetbite.delivery.infrastructure.inbound.rest.response.AutoAssignmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface AssignmentHttpMapper {

	@Mapping(target = "orderId", source = "orderId")
	@Mapping(target = "driverId", source = "request.driverId")
	CreateManualAssignmentCommand toCommand(UUID orderId, CreateManualAssignmentRequest request);

	RejectAssignmentCommand toCommand(RejectAssignmentRequest request);

	AssignmentResponse toResponse(AssignmentResult result);

	AutoAssignmentResponse toResponse(AutoAssignmentResult result);
}
