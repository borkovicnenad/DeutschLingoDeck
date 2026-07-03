package com.deutschlingodeck.common.exception;

/**
 * Signals that a requested resource does not exist. Maps to HTTP 404.
 */
public class ResourceNotFoundException extends ApplicationException {

	private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

	public ResourceNotFoundException(String message) {
		super(message, ERROR_CODE);
	}

	public static ResourceNotFoundException of(String resourceName, Object identifier) {
		return new ResourceNotFoundException(resourceName + " not found with id: " + identifier);
	}
}
