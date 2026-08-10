package com.fleetbite.vehicle.application.port.in;

import java.util.UUID;

import com.fleetbite.vehicle.application.dto.UpdateVehicleCommand;
import com.fleetbite.vehicle.application.dto.VehicleResult;

public interface UpdateVehicleUseCase {

	VehicleResult execute(UUID vehicleId, UpdateVehicleCommand command);
}
