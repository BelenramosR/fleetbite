package com.fleetbite.vehicle.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;
import com.fleetbite.vehicle.domain.model.VehicleStatus;

public final class InvalidVehicleTransitionException extends DomainException {

	public InvalidVehicleTransitionException(VehicleStatus from, VehicleStatus to) {
		super(
				"INVALID_VEHICLE_TRANSITION",
				"The vehicle cannot transition from " + from + " to " + to);
	}
}
