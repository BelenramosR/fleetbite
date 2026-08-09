package com.fleetbite.vehicle.application.service;

import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.vehicle.application.dto.VehicleResult;
import com.fleetbite.vehicle.application.port.in.GetVehicleByIdUseCase;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.model.VehicleId;

import java.util.Objects;

public final class GetVehicleByIdService implements GetVehicleByIdUseCase {

	private final VehicleRepositoryPort vehicleRepositoryPort;

	public GetVehicleByIdService(VehicleRepositoryPort vehicleRepositoryPort) {
		this.vehicleRepositoryPort = Objects.requireNonNull(vehicleRepositoryPort, "vehicleRepositoryPort");
	}

	@Override
	public VehicleResult execute(VehicleId vehicleId) {
		Objects.requireNonNull(vehicleId, "vehicleId is required");
		return vehicleRepositoryPort.findById(vehicleId)
				.map(VehicleResult::from)
				.orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId.value()));
	}
}
