package com.fleetbite.vehicle.application.service;

import java.util.UUID;

import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.exception.VehicleAssignedToDriverException;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.vehicle.application.port.in.DeleteVehicleUseCase;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.model.Vehicle;

import java.util.Objects;

public final class DeleteVehicleService implements DeleteVehicleUseCase {

	private final VehicleRepositoryPort vehicleRepositoryPort;
	private final DriverRepositoryPort driverRepositoryPort;

	public DeleteVehicleService(
			VehicleRepositoryPort vehicleRepositoryPort,
			DriverRepositoryPort driverRepositoryPort) {
		this.vehicleRepositoryPort = Objects.requireNonNull(vehicleRepositoryPort, "vehicleRepositoryPort");
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort, "driverRepositoryPort");
	}

	@Override
	public void execute(UUID vehicleId) {
		Objects.requireNonNull(vehicleId, "vehicleId is required");

		Vehicle vehicle = vehicleRepositoryPort.findById(vehicleId)
				.orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId));

		if (driverRepositoryPort.findByVehicleId(vehicleId).isPresent()) {
			throw new VehicleAssignedToDriverException(vehicleId);
		}

		vehicle.ensureDeletable();
		vehicleRepositoryPort.deleteById(vehicleId);
	}
}
