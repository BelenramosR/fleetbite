package com.fleetbite.vehicle.application.port.in;

import java.util.UUID;

import com.fleetbite.vehicle.application.dto.VehicleResult;

public interface GetVehicleByIdUseCase {

	VehicleResult execute(UUID vehicleId);
}
