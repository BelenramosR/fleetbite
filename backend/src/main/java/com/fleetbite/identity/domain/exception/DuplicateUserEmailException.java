package com.fleetbite.identity.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;

public final class DuplicateUserEmailException extends DomainException {

	public DuplicateUserEmailException(String email) {
		super("DUPLICATE_USER_EMAIL", "A user with email '" + email + "' already exists");
	}
}
