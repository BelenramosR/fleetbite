package com.fleetbite.vehicle.application.service;

import java.util.UUID;

import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.vehicle.application.dto.VehicleResult;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;

import java.util.Objects;

public final class GetVehicleByIdService {

	private final VehicleRepositoryPort vehicleRepositoryPort;

	public GetVehicleByIdService(VehicleRepositoryPort vehicleRepositoryPort) {
		this.vehicleRepositoryPort = Objects.requireNonNull(vehicleRepositoryPort, "vehicleRepositoryPort");
	}

	public VehicleResult execute(UUID vehicleId) {
		Objects.requireNonNull(vehicleId, "vehicleId is required");
		return vehicleRepositoryPort.findById(vehicleId)
				.map(VehicleResult::from)
				.orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId));
	}
}
