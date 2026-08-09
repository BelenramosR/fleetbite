package com.fleetbite.vehicle.infrastructure.inbound.rest;

import com.fleetbite.vehicle.application.dto.CreateVehicleCommand;
import com.fleetbite.vehicle.application.dto.UpdateVehicleCommand;
import com.fleetbite.vehicle.application.dto.VehicleResult;
import com.fleetbite.vehicle.infrastructure.inbound.rest.request.CreateVehicleRequest;
import com.fleetbite.vehicle.infrastructure.inbound.rest.request.UpdateVehicleRequest;
import com.fleetbite.vehicle.infrastructure.inbound.rest.response.VehicleResponse;
import org.springframework.stereotype.Component;

@Component
public class VehicleHttpMapper {

	public CreateVehicleCommand toCommand(CreateVehicleRequest request) {
		return new CreateVehicleCommand(request.plate(), request.type());
	}

	public UpdateVehicleCommand toCommand(UpdateVehicleRequest request) {
		return new UpdateVehicleCommand(request.plate(), request.type());
	}

	public VehicleResponse toResponse(VehicleResult result) {
		return new VehicleResponse(
				result.id(),
				result.plate(),
				result.type().name(),
				result.status().name(),
				result.createdAt(),
				result.updatedAt());
	}
}
