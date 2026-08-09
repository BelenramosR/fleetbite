package com.fleetbite.driver.domain.exception;

import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.shared.domain.exception.DomainException;

public final class DriverNotDeletableException extends DomainException {

	public DriverNotDeletableException(DriverStatus status) {
		super(
				"DRIVER_NOT_DELETABLE",
				"Driver cannot be deleted while in status " + status);
	}
}
