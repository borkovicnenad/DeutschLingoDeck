package com.deutschlingodeck.common.exception;

/**
 * Signals that the caller is not authenticated. Maps to HTTP 401.
 *
 * TODO: wire this up once JWT authentication is implemented (e.g. thrown by
 * the authentication service on invalid credentials or expired tokens).
 */
public class UnauthorizedException extends ApplicationException {

	private static final String ERROR_CODE = "UNAUTHORIZED";

	public UnauthorizedException(String message) {
		super(message, ERROR_CODE);
	}
}
