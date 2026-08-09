package com.fleetbite.delivery.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;

public final class InvalidAssignmentDataException extends DomainException {

	public InvalidAssignmentDataException(String message) {
		super("INVALID_ASSIGNMENT_DATA", message);
	}
}
