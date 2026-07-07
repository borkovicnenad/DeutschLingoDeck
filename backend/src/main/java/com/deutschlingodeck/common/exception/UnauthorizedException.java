package com.deutschlingodeck.common.exception;

/**
 * Signals that the caller is not authenticated. Maps to HTTP 401.
 */
public class UnauthorizedException extends ApplicationException {

	private static final String ERROR_CODE = "UNAUTHORIZED";

	public UnauthorizedException(String message) {
		super(message, ERROR_CODE);
	}
}
