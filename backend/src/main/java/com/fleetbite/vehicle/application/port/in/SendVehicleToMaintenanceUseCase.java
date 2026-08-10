package com.fleetbite.vehicle.application.port.in;

import java.util.UUID;

import com.fleetbite.vehicle.application.dto.VehicleResult;

public interface SendVehicleToMaintenanceUseCase {

	VehicleResult execute(UUID vehicleId);
}
