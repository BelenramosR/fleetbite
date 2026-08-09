package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.in.GetDriverByIdUseCase;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;

import java.util.Objects;

public final class GetDriverByIdService implements GetDriverByIdUseCase {

	private final DriverRepositoryPort driverRepositoryPort;
	private final DriverResultAssembler resultAssembler;

	public GetDriverByIdService(
			DriverRepositoryPort driverRepositoryPort,
			UserRepositoryPort userRepositoryPort,
			VehicleRepositoryPort vehicleRepositoryPort) {
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort, "driverRepositoryPort");
		this.resultAssembler = new DriverResultAssembler(userRepositoryPort, vehicleRepositoryPort);
	}

	@Override
	public DriverResult execute(DriverId driverId) {
		Objects.requireNonNull(driverId, "driverId is required");
		return driverRepositoryPort.findById(driverId)
				.map(resultAssembler::toResult)
				.orElseThrow(() -> new ResourceNotFoundException("Driver", driverId.value()));
	}
}
