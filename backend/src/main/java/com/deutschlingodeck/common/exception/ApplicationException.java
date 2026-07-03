package com.deutschlingodeck.common.exception;

/**
 * Base type for all application-level exceptions that carry a stable,
 * machine-readable error code alongside the human-readable message.
 */
public abstract class ApplicationException extends RuntimeException {

	private final String errorCode;

	protected ApplicationException(String message, String errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	protected ApplicationException(String message, String errorCode, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}
}
