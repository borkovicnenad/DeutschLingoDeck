package com.deutschlingodeck.common.response;

/**
 * Generic success envelope available for internal or future endpoints that
 * are not bound to a fixed OpenAPI response schema.
 *
 * <p>Controllers generated from {@code docs/openapi.yaml} return their DTOs
 * directly (per the spec) rather than wrapping them in this type.
 */
public record ApiResponse<T>(
		T data,
		String message
) {

	public static <T> ApiResponse<T> of(T data) {
		return new ApiResponse<>(data, null);
	}

	public static <T> ApiResponse<T> of(T data, String message) {
		return new ApiResponse<>(data, message);
	}
}
