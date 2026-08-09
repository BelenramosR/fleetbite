package com.fleetbite.driver.domain.exception;

import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.shared.domain.exception.DomainException;

public final class InvalidDriverTransitionException extends DomainException {

	public InvalidDriverTransitionException(DriverStatus from, DriverStatus to) {
		super(
				"INVALID_DRIVER_TRANSITION",
				"The driver cannot transition from " + from + " to " + to);
	}

	public InvalidDriverTransitionException(String message) {
		super("INVALID_DRIVER_TRANSITION", message);
	}
}
