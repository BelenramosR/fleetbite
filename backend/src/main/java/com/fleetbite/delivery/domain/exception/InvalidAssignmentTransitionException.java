package com.fleetbite.delivery.domain.exception;

import com.fleetbite.delivery.domain.model.AssignmentStatus;
import com.fleetbite.shared.domain.exception.DomainException;

public final class InvalidAssignmentTransitionException extends DomainException {

	public InvalidAssignmentTransitionException(AssignmentStatus from, AssignmentStatus to) {
		super(
				"INVALID_ASSIGNMENT_TRANSITION",
				"The assignment cannot transition from " + from + " to " + to);
	}

	public InvalidAssignmentTransitionException(String message) {
		super("INVALID_ASSIGNMENT_TRANSITION", message);
	}
}
