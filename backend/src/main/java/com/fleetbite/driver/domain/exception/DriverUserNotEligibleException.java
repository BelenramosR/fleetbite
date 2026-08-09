package com.fleetbite.driver.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;

import java.util.UUID;

public final class DriverUserNotEligibleException extends DomainException {

	public DriverUserNotEligibleException(UUID userId) {
		super(
				"DRIVER_USER_NOT_ELIGIBLE",
				"User '" + userId + "' is not eligible to be linked as a driver");
	}
}
