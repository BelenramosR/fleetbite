package com.fleetbite.driver.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;

public final class InvalidDriverDataException extends DomainException {

	public InvalidDriverDataException(String message) {
		super("INVALID_DRIVER_DATA", message);
	}
}
