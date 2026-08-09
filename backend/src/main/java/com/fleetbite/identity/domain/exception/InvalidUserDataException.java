package com.fleetbite.identity.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;

public final class InvalidUserDataException extends DomainException {

	public InvalidUserDataException(String message) {
		super("INVALID_USER_DATA", message);
	}
}
