package com.fleetbite.shared.application.exception;

/**
 * Raised when a requested aggregate or resource cannot be resolved.
 */
public class ResourceNotFoundException extends ApplicationException {

	public ResourceNotFoundException(String resourceType, Object identifier) {
		super("RESOURCE_NOT_FOUND", resourceType + " not found: " + identifier);
	}
}
