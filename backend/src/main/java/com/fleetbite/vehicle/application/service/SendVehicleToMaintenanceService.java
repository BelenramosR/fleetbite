package com.fleetbite.vehicle.application.service;

import java.util.UUID;

import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.dto.VehicleResult;
import com.fleetbite.vehicle.application.port.in.SendVehicleToMaintenanceUseCase;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.model.Vehicle;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class SendVehicleToMaintenanceService implements SendVehicleToMaintenanceUseCase {

	private final VehicleRepositoryPort vehicleRepositoryPort;
	private final Clock clock;

	public SendVehicleToMaintenanceService(VehicleRepositoryPort vehicleRepositoryPort, Clock clock) {
		this.vehicleRepositoryPort = Objects.requireNonNull(vehicleRepositoryPort, "vehicleRepositoryPort");
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public VehicleResult execute(UUID vehicleId) {
		Objects.requireNonNull(vehicleId, "vehicleId is required");

		Vehicle vehicle = vehicleRepositoryPort.findById(vehicleId)
				.orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId));

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		vehicle.sendToMaintenance(now);

		Vehicle updated = vehicleRepositoryPort.update(vehicle);
		return VehicleResult.from(updated);
	}
}
