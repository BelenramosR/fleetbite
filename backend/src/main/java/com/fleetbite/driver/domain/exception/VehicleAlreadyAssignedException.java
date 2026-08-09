package com.fleetbite.driver.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;

import java.util.UUID;

public final class VehicleAlreadyAssignedException extends DomainException {

	public VehicleAlreadyAssignedException(UUID vehicleId) {
		super(
				"VEHICLE_ALREADY_ASSIGNED",
				"Vehicle '" + vehicleId + "' is already assigned to a driver");
	}
}
