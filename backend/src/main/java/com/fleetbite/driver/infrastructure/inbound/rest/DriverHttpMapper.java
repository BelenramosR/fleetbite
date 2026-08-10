package com.fleetbite.driver.infrastructure.inbound.rest;

import com.fleetbite.driver.application.dto.AssignVehicleToDriverCommand;
import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.dto.UpdateDriverCommand;
import com.fleetbite.driver.application.dto.UpdateDriverLocationCommand;
import com.fleetbite.driver.application.dto.VehicleSummary;
import com.fleetbite.driver.infrastructure.inbound.rest.request.AssignVehicleRequest;
import com.fleetbite.driver.infrastructure.inbound.rest.request.UpdateDriverLocationRequest;
import com.fleetbite.driver.infrastructure.inbound.rest.request.UpdateDriverRequest;
import com.fleetbite.driver.infrastructure.inbound.rest.response.DriverResponse;
import com.fleetbite.driver.infrastructure.inbound.rest.response.DriverVehicleResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DriverHttpMapper {

	UpdateDriverCommand toCommand(UpdateDriverRequest request);

	UpdateDriverLocationCommand toCommand(UpdateDriverLocationRequest request);

	AssignVehicleToDriverCommand toCommand(AssignVehicleRequest request);

	DriverResponse toResponse(DriverResult result);

	default DriverVehicleResponse toVehicleResponse(VehicleSummary summary) {
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
