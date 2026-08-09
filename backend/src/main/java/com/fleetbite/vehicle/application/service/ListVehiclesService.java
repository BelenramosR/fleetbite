package com.fleetbite.vehicle.application.service;

import com.fleetbite.vehicle.application.dto.VehicleResult;
import com.fleetbite.vehicle.application.port.in.ListVehiclesUseCase;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;

import java.util.List;
import java.util.Objects;

public final class ListVehiclesService implements ListVehiclesUseCase {

	private final VehicleRepositoryPort vehicleRepositoryPort;

	public ListVehiclesService(VehicleRepositoryPort vehicleRepositoryPort) {
		this.vehicleRepositoryPort = Objects.requireNonNull(vehicleRepositoryPort, "vehicleRepositoryPort");
	}

	@Override
	public List<VehicleResult> execute() {
		return vehicleRepositoryPort.findAll().stream()
				.map(VehicleResult::from)
				.toList();
	}
}
