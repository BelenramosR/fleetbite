package com.fleetbite.vehicle.application.port.in;

import com.fleetbite.vehicle.application.dto.VehicleResult;
import java.util.UUID;

public interface VehicleLifecycleUseCase {
	VehicleResult sendToMaintenance(UUID vehicleId);
	VehicleResult activate(UUID vehicleId);
	VehicleResult deactivate(UUID vehicleId);
}
