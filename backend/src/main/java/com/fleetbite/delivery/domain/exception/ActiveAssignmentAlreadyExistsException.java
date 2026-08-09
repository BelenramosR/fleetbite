package com.fleetbite.delivery.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;

import java.util.UUID;

public final class ActiveAssignmentAlreadyExistsException extends DomainException {

	public ActiveAssignmentAlreadyExistsException(UUID orderId) {
		super(
				"ACTIVE_ASSIGNMENT_EXISTS",
				"Order " + orderId + " already has an active assignment");
	}
}
