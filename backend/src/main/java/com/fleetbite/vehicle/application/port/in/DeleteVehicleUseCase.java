package com.fleetbite.vehicle.application.port.in;

import java.util.UUID;


public interface DeleteVehicleUseCase {

	void execute(UUID vehicleId);
}
