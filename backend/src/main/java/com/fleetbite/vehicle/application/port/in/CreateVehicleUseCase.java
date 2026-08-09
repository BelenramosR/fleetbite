package com.fleetbite.vehicle.application.port.in;

import com.fleetbite.vehicle.application.dto.CreateVehicleCommand;
import com.fleetbite.vehicle.application.dto.VehicleResult;

public interface CreateVehicleUseCase {

	VehicleResult execute(CreateVehicleCommand command);
}
