package com.deutschlingodeck.common.exception;

/**
 * Signals that a request conflicts with the current state of a resource
 * (e.g. duplicate unique field, invalid state transition). Maps to HTTP 409.
 */
public class ConflictException extends ApplicationException {

	private static final String ERROR_CODE = "CONFLICT";

	public ConflictException(String message) {
		super(message, ERROR_CODE);
	}
}
