package com.fleetbite.identity.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;

public final class AuthenticationFailedException extends DomainException {

	public AuthenticationFailedException() {
		super("AUTHENTICATION_FAILED", "Invalid email or password");
	}
}
