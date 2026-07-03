package com.deutschlingodeck.common.response;

/**
 * A single field-level validation failure, matching the {@code FieldError}
 * schema in {@code docs/openapi.yaml}.
 */
public record FieldError(
		String field,
		String message
) {
}
