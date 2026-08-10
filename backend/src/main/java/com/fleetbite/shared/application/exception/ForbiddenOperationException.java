package com.fleetbite.shared.application.exception;

public class ForbiddenOperationException extends ApplicationException {

	public ForbiddenOperationException(String message) {
		super("ACCESS_DENIED", message);
	}
}
