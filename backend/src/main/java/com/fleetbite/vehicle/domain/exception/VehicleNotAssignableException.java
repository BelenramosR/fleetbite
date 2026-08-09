package com.fleetbite.vehicle.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;
import com.fleetbite.vehicle.domain.model.VehicleStatus;

public class VehicleNotAssignableException extends DomainException {

	public VehicleNotAssignableException(VehicleStatus status) {
		super(
				"VEHICLE_NOT_ASSIGNABLE",
				"Vehicle cannot be assigned when status is " + status);
	}
}
