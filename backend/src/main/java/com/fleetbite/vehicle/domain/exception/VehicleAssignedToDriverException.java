package com.fleetbite.vehicle.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;

import java.util.UUID;

public final class VehicleAssignedToDriverException extends DomainException {

	public VehicleAssignedToDriverException(UUID vehicleId) {
		super(
				"VEHICLE_ASSIGNED_TO_DRIVER",
				"Vehicle '" + vehicleId + "' is assigned to a driver and cannot be deleted");
	}
}
