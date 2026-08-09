package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.dto.VehicleSummary;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;

import java.util.Objects;

final class DriverResultAssembler {

	private final UserRepositoryPort userRepositoryPort;
	private final VehicleRepositoryPort vehicleRepositoryPort;

	DriverResultAssembler(UserRepositoryPort userRepositoryPort, VehicleRepositoryPort vehicleRepositoryPort) {
		this.userRepositoryPort = Objects.requireNonNull(userRepositoryPort, "userRepositoryPort");
		this.vehicleRepositoryPort = Objects.requireNonNull(vehicleRepositoryPort, "vehicleRepositoryPort");
	}

	DriverResult toResult(Driver driver) {
		Objects.requireNonNull(driver, "driver is required");
		User user = userRepositoryPort.findById(driver.userId())
				.orElseThrow(() -> new ResourceNotFoundException("User", driver.userId().value()));
		VehicleSummary summary = null;
		if (driver.vehicleId() != null) {
			summary = vehicleRepositoryPort.findById(driver.vehicleId())
					.map(VehicleSummary::from)
					.orElse(null);
		}
		return DriverResult.from(driver, user.fullName(), summary);
	}
}
