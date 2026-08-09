package com.fleetbite.vehicle.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;

public final class DuplicateVehiclePlateException extends DomainException {

	public DuplicateVehiclePlateException(String plate) {
		super(
				"DUPLICATE_VEHICLE_PLATE",
				"A vehicle with plate '" + plate + "' already exists");
	}
}
