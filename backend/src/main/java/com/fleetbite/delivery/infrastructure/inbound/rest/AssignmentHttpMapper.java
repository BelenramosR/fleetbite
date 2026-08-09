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

	@Mapping(target = "id", expression = "java(result.id())")
	@Mapping(target = "orderId", expression = "java(result.orderId())")
	@Mapping(target = "driverId", expression = "java(result.driverId())")
	@Mapping(target = "status", expression = "java(result.status().name())")
	@Mapping(target = "assignedAt", expression = "java(result.assignedAt())")
	@Mapping(target = "acceptedAt", expression = "java(result.acceptedAt())")
	@Mapping(target = "rejectedAt", expression = "java(result.rejectedAt())")
	@Mapping(target = "pickedUpAt", expression = "java(result.pickedUpAt())")
	@Mapping(target = "completedAt", expression = "java(result.completedAt())")
	@Mapping(target = "rejectionReason", expression = "java(result.rejectionReason())")
	@Mapping(target = "assignmentScore", expression = "java(result.assignmentScore())")
	@Mapping(target = "createdAt", expression = "java(result.createdAt())")
	AssignmentResponse toResponse(AssignmentResult result);

	@Mapping(target = "orderStatus", expression = "java(result.orderStatus().name())")
	AutoAssignmentResponse toResponse(AutoAssignmentResult result);
}
