package com.fleetbite.driver.infrastructure.inbound.rest;

import com.fleetbite.driver.application.dto.AssignVehicleToDriverCommand;
import com.fleetbite.driver.application.dto.CreateDriverCommand;
import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.dto.UpdateDriverCommand;
import com.fleetbite.driver.application.dto.UpdateDriverLocationCommand;
import com.fleetbite.driver.application.dto.VehicleSummary;
import com.fleetbite.driver.infrastructure.inbound.rest.request.AssignVehicleRequest;
import com.fleetbite.driver.infrastructure.inbound.rest.request.CreateDriverRequest;
import com.fleetbite.driver.infrastructure.inbound.rest.request.UpdateDriverLocationRequest;
import com.fleetbite.driver.infrastructure.inbound.rest.request.UpdateDriverRequest;
import com.fleetbite.driver.infrastructure.inbound.rest.response.DriverResponse;
import com.fleetbite.driver.infrastructure.inbound.rest.response.DriverVehicleResponse;
import org.springframework.stereotype.Component;

@Component
public class DriverHttpMapper {

	public CreateDriverCommand toCommand(CreateDriverRequest request) {
		return new CreateDriverCommand(
				request.userId(),
				request.phone(),
				request.currentLatitude(),
				request.currentLongitude());
	}

	public UpdateDriverCommand toCommand(UpdateDriverRequest request) {
		return new UpdateDriverCommand(request.phone());
	}

	public UpdateDriverLocationCommand toCommand(UpdateDriverLocationRequest request) {
		return new UpdateDriverLocationCommand(request.latitude(), request.longitude());
	}

	public AssignVehicleToDriverCommand toCommand(AssignVehicleRequest request) {
		return new AssignVehicleToDriverCommand(request.vehicleId());
	}

	public DriverResponse toResponse(DriverResult result) {
		return new DriverResponse(
				result.id(),
				result.userId(),
				result.name(),
				result.phone(),
				result.status().name(),
				result.currentLatitude(),
				result.currentLongitude(),
				result.vehicleId(),
				toVehicleResponse(result.vehicle()),
				result.createdAt(),
				result.updatedAt());
	}

	private static DriverVehicleResponse toVehicleResponse(VehicleSummary summary) {
		if (summary == null) {
			return null;
		}
		return new DriverVehicleResponse(
				summary.id(),
				summary.plate(),
				summary.type().name(),
				summary.status().name());
	}
}
