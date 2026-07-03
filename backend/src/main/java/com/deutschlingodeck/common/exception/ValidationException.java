package com.deutschlingodeck.common.exception;

import com.deutschlingodeck.common.response.FieldError;

import java.util.List;

/**
 * Signals that request data failed validation outside of standard Bean
 * Validation (e.g. cross-field or business-level checks). Maps to HTTP 400.
 */
public class ValidationException extends ApplicationException {

	private static final String ERROR_CODE = "VALIDATION_ERROR";

	private final List<FieldError> errors;

	public ValidationException(String message, List<FieldError> errors) {
		super(message, ERROR_CODE);
		this.errors = errors;
	}

	public ValidationException(String message) {
		this(message, List.of());
	}

	public List<FieldError> getErrors() {
		return errors;
	}
}
