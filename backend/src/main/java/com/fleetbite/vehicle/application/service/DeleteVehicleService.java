package com.fleetbite.vehicle.application.service;

import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.exception.VehicleAssignedToDriverException;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.vehicle.application.port.in.DeleteVehicleUseCase;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleId;

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
	public void execute(VehicleId vehicleId) {
		Objects.requireNonNull(vehicleId, "vehicleId is required");

		Vehicle vehicle = vehicleRepositoryPort.findById(vehicleId)
				.orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId.value()));

		if (driverRepositoryPort.findByVehicleId(vehicleId).isPresent()) {
			throw new VehicleAssignedToDriverException(vehicleId.value());
		}

		vehicle.ensureDeletable();
		vehicleRepositoryPort.deleteById(vehicleId);
	}
}
