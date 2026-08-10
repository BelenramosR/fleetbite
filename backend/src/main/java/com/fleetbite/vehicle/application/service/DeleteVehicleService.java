package com.fleetbite.vehicle.application.service;

import java.util.UUID;

import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.vehicle.application.port.in.DeleteVehicleUseCase;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.application.port.out.VehicleAssignmentLookupPort;
import com.fleetbite.vehicle.domain.exception.VehicleAssignedToDriverException;
import com.fleetbite.vehicle.domain.model.Vehicle;

import java.util.Objects;

public final class DeleteVehicleService implements DeleteVehicleUseCase {

	private final VehicleRepositoryPort vehicleRepositoryPort;
	private final VehicleAssignmentLookupPort assignmentLookupPort;

	public DeleteVehicleService(
			VehicleRepositoryPort vehicleRepositoryPort,
			VehicleAssignmentLookupPort assignmentLookupPort) {
		this.vehicleRepositoryPort = Objects.requireNonNull(vehicleRepositoryPort, "vehicleRepositoryPort");
		this.assignmentLookupPort = Objects.requireNonNull(assignmentLookupPort, "assignmentLookupPort");
	}

	@Override
	public void execute(UUID vehicleId) {
		Objects.requireNonNull(vehicleId, "vehicleId is required");

		Vehicle vehicle = vehicleRepositoryPort.findById(vehicleId)
				.orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId));

		if (assignmentLookupPort.isAssigned(vehicleId)) {
			throw new VehicleAssignedToDriverException(vehicleId);
		}

		vehicle.ensureDeletable();
		vehicleRepositoryPort.deleteById(vehicleId);
	}
}
