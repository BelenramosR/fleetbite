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
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DriverHttpMapper {

	UpdateDriverCommand toCommand(UpdateDriverRequest request);

	UpdateDriverLocationCommand toCommand(UpdateDriverLocationRequest request);

	AssignVehicleToDriverCommand toCommand(AssignVehicleRequest request);

	@Mapping(target = "id", expression = "java(result.id())")
	@Mapping(target = "userId", expression = "java(result.userId())")
	@Mapping(target = "name", expression = "java(result.name())")
	@Mapping(target = "phone", expression = "java(result.phone())")
	@Mapping(target = "status", expression = "java(result.status().name())")
	@Mapping(target = "currentLatitude", expression = "java(result.currentLatitude())")
	@Mapping(target = "currentLongitude", expression = "java(result.currentLongitude())")
	@Mapping(target = "vehicleId", expression = "java(result.vehicleId())")
	@Mapping(target = "vehicle", expression = "java(toVehicleResponse(result.vehicle()))")
	@Mapping(target = "createdAt", expression = "java(result.createdAt())")
	@Mapping(target = "updatedAt", expression = "java(result.updatedAt())")
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
