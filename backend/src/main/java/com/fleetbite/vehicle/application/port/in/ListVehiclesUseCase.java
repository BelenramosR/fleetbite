package com.fleetbite.vehicle.application.port.in;

import com.fleetbite.vehicle.application.dto.VehicleResult;

import java.util.List;

public interface ListVehiclesUseCase {

	List<VehicleResult> execute();
}
