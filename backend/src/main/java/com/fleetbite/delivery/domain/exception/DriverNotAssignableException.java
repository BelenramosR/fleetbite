package com.fleetbite.delivery.domain.exception;

import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.shared.domain.exception.DomainException;

public final class DriverNotAssignableException extends DomainException {

	public DriverNotAssignableException(DriverStatus status) {
		super(
				"DRIVER_NOT_ASSIGNABLE",
				"Driver cannot be assigned while in status " + status);
	}

	public DriverNotAssignableException(String message) {
		super("DRIVER_NOT_ASSIGNABLE", message);
	}
}
