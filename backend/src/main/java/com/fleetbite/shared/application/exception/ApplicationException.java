package com.fleetbite.shared.application.exception;

/**
 * Base exception for application-layer failures (orchestration, not found, conflicts).
 */
public class ApplicationException extends RuntimeException {

	private final String code;

	public ApplicationException(String code, String message) {
		super(message);
		this.code = code;
	}

	public ApplicationException(String code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
