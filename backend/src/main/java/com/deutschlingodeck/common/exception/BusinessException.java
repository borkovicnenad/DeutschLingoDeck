package com.deutschlingodeck.common.exception;

/**
 * Signals a violation of a business rule that is not covered by a more
 * specific exception type. Maps to HTTP 400 by default.
 */
public class BusinessException extends ApplicationException {

	private static final String ERROR_CODE = "BUSINESS_RULE_VIOLATION";

	public BusinessException(String message) {
		super(message, ERROR_CODE);
	}

	public BusinessException(String message, String errorCode) {
		super(message, errorCode);
	}
}
