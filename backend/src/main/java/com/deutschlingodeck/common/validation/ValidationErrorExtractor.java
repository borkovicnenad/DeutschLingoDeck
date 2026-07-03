package com.deutschlingodeck.common.validation;

import com.deutschlingodeck.common.response.FieldError;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.List;

/**
 * Converts Spring's {@link BindingResult} field errors into the API's
 * {@link FieldError} shape, used by the global exception handler when
 * Bean Validation fails on a request body.
 */
public final class ValidationErrorExtractor {

	private ValidationErrorExtractor() {
	}

	public static List<FieldError> extract(BindingResult bindingResult) {
		return bindingResult.getAllErrors().stream()
				.map(ValidationErrorExtractor::toFieldError)
				.toList();
	}

	private static FieldError toFieldError(ObjectError error) {
		String field = error instanceof org.springframework.validation.FieldError fe ? fe.getField() : error.getObjectName();
		return new FieldError(field, error.getDefaultMessage());
	}
}
