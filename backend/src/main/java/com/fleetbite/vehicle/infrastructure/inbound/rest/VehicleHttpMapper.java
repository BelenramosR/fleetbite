package com.fleetbite.vehicle.infrastructure.inbound.rest;

import com.fleetbite.vehicle.application.dto.CreateVehicleCommand;
import com.fleetbite.vehicle.application.dto.UpdateVehicleCommand;
import com.fleetbite.vehicle.application.dto.VehicleResult;
import com.fleetbite.vehicle.infrastructure.inbound.rest.request.CreateVehicleRequest;
import com.fleetbite.vehicle.infrastructure.inbound.rest.request.UpdateVehicleRequest;
import com.fleetbite.vehicle.infrastructure.inbound.rest.response.VehicleResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VehicleHttpMapper {

	CreateVehicleCommand toCommand(CreateVehicleRequest request);

	UpdateVehicleCommand toCommand(UpdateVehicleRequest request);

	VehicleResponse toResponse(VehicleResult result);
}
