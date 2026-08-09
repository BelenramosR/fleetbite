package com.fleetbite.vehicle.application.dto;

import com.fleetbite.vehicle.domain.model.VehicleType;

public record UpdateVehicleCommand(String plate, VehicleType type) {
}
