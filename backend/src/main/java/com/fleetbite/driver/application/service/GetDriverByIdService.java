package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.in.GetDriverByIdUseCase;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;

import java.util.Objects;

public final class GetDriverByIdService implements GetDriverByIdUseCase {

	private final DriverRepositoryPort driverRepositoryPort;

	public GetDriverByIdService(DriverRepositoryPort driverRepositoryPort) {
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort, "driverRepositoryPort");
	}

	@Override
	public DriverResult execute(DriverId driverId) {
		Objects.requireNonNull(driverId, "driverId is required");
		return driverRepositoryPort.findById(driverId)
				.map(DriverResult::from)
				.orElseThrow(() -> new ResourceNotFoundException("Driver", driverId.value()));
	}
}
