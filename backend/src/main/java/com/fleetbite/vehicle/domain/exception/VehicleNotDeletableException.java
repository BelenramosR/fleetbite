package com.fleetbite.vehicle.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;
import com.fleetbite.vehicle.domain.model.VehicleStatus;

public final class VehicleNotDeletableException extends DomainException {

	public VehicleNotDeletableException(VehicleStatus status) {
		super(
				"VEHICLE_NOT_DELETABLE",
				"Vehicle cannot be deleted while in status " + status);
	}
}
