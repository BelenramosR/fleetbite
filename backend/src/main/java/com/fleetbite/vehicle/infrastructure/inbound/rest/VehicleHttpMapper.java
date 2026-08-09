package com.fleetbite.vehicle.infrastructure.inbound.rest;

import com.fleetbite.vehicle.application.dto.CreateVehicleCommand;
import com.fleetbite.vehicle.application.dto.UpdateVehicleCommand;
import com.fleetbite.vehicle.application.dto.VehicleResult;
import com.fleetbite.vehicle.infrastructure.inbound.rest.request.CreateVehicleRequest;
import com.fleetbite.vehicle.infrastructure.inbound.rest.request.UpdateVehicleRequest;
import com.fleetbite.vehicle.infrastructure.inbound.rest.response.VehicleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehicleHttpMapper {

	CreateVehicleCommand toCommand(CreateVehicleRequest request);

	UpdateVehicleCommand toCommand(UpdateVehicleRequest request);

	@Mapping(target = "id", expression = "java(result.id())")
	@Mapping(target = "plate", expression = "java(result.plate())")
	@Mapping(target = "type", expression = "java(result.type().name())")
	@Mapping(target = "status", expression = "java(result.status().name())")
	@Mapping(target = "createdAt", expression = "java(result.createdAt())")
	@Mapping(target = "updatedAt", expression = "java(result.updatedAt())")
	VehicleResponse toResponse(VehicleResult result);
}
