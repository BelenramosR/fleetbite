package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.port.in.DeleteDriverUseCase;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;

import java.util.Objects;

public final class DeleteDriverService implements DeleteDriverUseCase {

	private final DriverRepositoryPort driverRepositoryPort;

	public DeleteDriverService(DriverRepositoryPort driverRepositoryPort) {
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort, "driverRepositoryPort");
	}

	@Override
	public void execute(DriverId driverId) {
		Objects.requireNonNull(driverId, "driverId is required");

		Driver driver = driverRepositoryPort.findById(driverId)
				.orElseThrow(() -> new ResourceNotFoundException("Driver", driverId.value()));

		driver.ensureDeletable();
		driverRepositoryPort.deleteById(driverId);
	}
}
