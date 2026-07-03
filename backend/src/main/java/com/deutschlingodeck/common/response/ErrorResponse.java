package com.deutschlingodeck.common.response;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Standard error payload returned by {@link com.deutschlingodeck.common.exception.GlobalExceptionHandler},
 * matching the {@code ErrorResponse} schema in {@code docs/openapi.yaml}.
 */
public record ErrorResponse(
		OffsetDateTime timestamp,
		int status,
		String code,
		String message,
		String correlationId,
		List<FieldError> errors
) {

	public static ErrorResponse of(int status, String code, String message, String correlationId) {
		return new ErrorResponse(OffsetDateTime.now(), status, code, message, correlationId, List.of());
	}

	public static ErrorResponse of(int status, String code, String message, String correlationId, List<FieldError> errors) {
		return new ErrorResponse(OffsetDateTime.now(), status, code, message, correlationId, errors);
	}
}
