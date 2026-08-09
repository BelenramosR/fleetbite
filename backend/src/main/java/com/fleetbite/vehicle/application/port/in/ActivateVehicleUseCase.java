package com.fleetbite.vehicle.application.port.in;

import com.fleetbite.vehicle.application.dto.VehicleResult;
import com.fleetbite.vehicle.domain.model.VehicleId;

public interface ActivateVehicleUseCase {

	VehicleResult execute(VehicleId vehicleId);
}
