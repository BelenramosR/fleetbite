package com.fleetbite.vehicle.application.port.in;

import com.fleetbite.vehicle.domain.model.VehicleId;

public interface DeleteVehicleUseCase {

	void execute(VehicleId vehicleId);
}
