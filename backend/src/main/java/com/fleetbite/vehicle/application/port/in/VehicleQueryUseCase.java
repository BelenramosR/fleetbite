package com.fleetbite.vehicle.application.port.in;

import com.fleetbite.vehicle.application.dto.VehicleResult;
import java.util.List;
import java.util.UUID;

public interface VehicleQueryUseCase {
	VehicleResult getById(UUID vehicleId);
	List<VehicleResult> findAll();
}
