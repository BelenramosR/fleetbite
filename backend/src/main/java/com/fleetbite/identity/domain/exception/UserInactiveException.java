package com.fleetbite.identity.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;

public final class UserInactiveException extends DomainException {

	public UserInactiveException() {
		super("USER_INACTIVE", "User account is inactive");
	}
}
