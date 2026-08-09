package com.fleetbite.driver.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;

import java.util.UUID;

public final class DriverAlreadyLinkedToUserException extends DomainException {

	public DriverAlreadyLinkedToUserException(UUID userId) {
		super(
				"DRIVER_ALREADY_LINKED_TO_USER",
				"A driver is already linked to user '" + userId + "'");
	}
}
