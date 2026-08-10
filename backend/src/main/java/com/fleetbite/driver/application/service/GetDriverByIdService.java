package com.fleetbite.driver.application.service;

import java.util.UUID;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;

import java.util.Objects;

public final class GetDriverByIdService {

	private final DriverRepositoryPort driverRepositoryPort;
	private final DriverResultAssembler resultAssembler;

	public GetDriverByIdService(
			DriverRepositoryPort driverRepositoryPort,
			UserRepositoryPort userRepositoryPort,
			VehicleRepositoryPort vehicleRepositoryPort) {
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort, "driverRepositoryPort");
		this.resultAssembler = new DriverResultAssembler(userRepositoryPort, vehicleRepositoryPort);
	}

	public DriverResult execute(UUID driverId) {
		Objects.requireNonNull(driverId, "driverId is required");
		return driverRepositoryPort.findById(driverId)
				.map(resultAssembler::toResult)
				.orElseThrow(() -> new ResourceNotFoundException("Driver", driverId));
	}
}
