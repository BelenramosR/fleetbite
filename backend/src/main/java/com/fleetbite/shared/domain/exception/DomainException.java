package com.fleetbite.shared.domain.exception;

/**
 * Base exception for domain rule violations.
 * Free of Spring and infrastructure dependencies.
 */
public abstract class DomainException extends RuntimeException {

	private final String code;

	protected DomainException(String code, String message) {
		super(message);
		this.code = code;
	}

	protected DomainException(String code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
