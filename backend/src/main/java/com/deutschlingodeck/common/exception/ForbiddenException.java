package com.deutschlingodeck.common.exception;

/**
 * Signals that the caller is authenticated but not allowed to perform the
 * requested operation. Maps to HTTP 403.
 *
 * TODO: wire this up once authorization rules are implemented.
 */
public class ForbiddenException extends ApplicationException {

	private static final String ERROR_CODE = "FORBIDDEN";

	public ForbiddenException(String message) {
		super(message, ERROR_CODE);
	}
}
