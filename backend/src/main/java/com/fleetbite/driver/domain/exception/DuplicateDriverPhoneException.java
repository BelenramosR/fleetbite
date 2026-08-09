package com.fleetbite.driver.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;

public final class DuplicateDriverPhoneException extends DomainException {

	public DuplicateDriverPhoneException(String phone) {
		super(
				"DUPLICATE_DRIVER_PHONE",
				"A driver with phone '" + phone + "' already exists");
	}
}
