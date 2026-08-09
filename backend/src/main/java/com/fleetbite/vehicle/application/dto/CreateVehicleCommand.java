package com.fleetbite.vehicle.application.dto;

import com.fleetbite.vehicle.domain.model.VehicleType;

public record CreateVehicleCommand(String plate, VehicleType type) {
}
