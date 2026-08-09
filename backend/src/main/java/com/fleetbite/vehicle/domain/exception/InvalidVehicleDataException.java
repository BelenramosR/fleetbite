package com.fleetbite.vehicle.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;

public final class InvalidVehicleDataException extends DomainException {

	public InvalidVehicleDataException(String message) {
		super("INVALID_VEHICLE_DATA", message);
	}
}
