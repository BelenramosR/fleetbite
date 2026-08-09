package com.fleetbite.driver.infrastructure.inbound.rest;

import com.fleetbite.driver.application.dto.CreateDriverCommand;
import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.dto.UpdateDriverCommand;
import com.fleetbite.driver.application.dto.UpdateDriverLocationCommand;
import com.fleetbite.driver.infrastructure.inbound.rest.request.CreateDriverRequest;
import com.fleetbite.driver.infrastructure.inbound.rest.request.UpdateDriverLocationRequest;
import com.fleetbite.driver.infrastructure.inbound.rest.request.UpdateDriverRequest;
import com.fleetbite.driver.infrastructure.inbound.rest.response.DriverResponse;
import org.springframework.stereotype.Component;

@Component
public class DriverHttpMapper {

	public CreateDriverCommand toCommand(CreateDriverRequest request) {
		return new CreateDriverCommand(
				request.name(),
				request.phone(),
				request.currentLatitude(),
				request.currentLongitude());
	}

	public UpdateDriverCommand toCommand(UpdateDriverRequest request) {
		return new UpdateDriverCommand(request.name(), request.phone());
	}

	public UpdateDriverLocationCommand toCommand(UpdateDriverLocationRequest request) {
		return new UpdateDriverLocationCommand(request.latitude(), request.longitude());
	}

	public DriverResponse toResponse(DriverResult result) {
		return new DriverResponse(
				result.id(),
				result.name(),
				result.phone(),
				result.status().name(),
				result.currentLatitude(),
				result.currentLongitude(),
				result.createdAt(),
				result.updatedAt());
	}
}
