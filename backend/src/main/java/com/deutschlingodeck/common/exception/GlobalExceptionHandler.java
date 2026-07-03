package com.deutschlingodeck.common.exception;

import com.deutschlingodeck.common.response.ErrorResponse;
import com.deutschlingodeck.common.validation.ValidationErrorExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

/**
 * Translates exceptions thrown anywhere in the application into the
 * {@link ErrorResponse} shape defined by {@code docs/openapi.yaml}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		String correlationId = newCorrelationId();
		ErrorResponse body = ErrorResponse.of(
				HttpStatus.BAD_REQUEST.value(),
				"VALIDATION_ERROR",
				"Request validation failed",
				correlationId,
				ValidationErrorExtractor.extract(ex.getBindingResult())
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
		String correlationId = newCorrelationId();
		ErrorResponse body = ErrorResponse.of(
				HttpStatus.BAD_REQUEST.value(), ex.getErrorCode(), ex.getMessage(), correlationId, ex.getErrors());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
		return errorResponse(HttpStatus.NOT_FOUND, ex);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
		return errorResponse(HttpStatus.CONFLICT, ex);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
		return errorResponse(HttpStatus.UNAUTHORIZED, ex);
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
		return errorResponse(HttpStatus.FORBIDDEN, ex);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
		return errorResponse(HttpStatus.BAD_REQUEST, ex);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
		String correlationId = newCorrelationId();
		log.error("Unhandled exception [correlationId={}]", correlationId, ex);
		ErrorResponse body = ErrorResponse.of(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"INTERNAL_ERROR",
				"An unexpected error occurred",
				correlationId
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

	private ResponseEntity<ErrorResponse> errorResponse(HttpStatus status, ApplicationException ex) {
		String correlationId = newCorrelationId();
		ErrorResponse body = ErrorResponse.of(status.value(), ex.getErrorCode(), ex.getMessage(), correlationId);
		return ResponseEntity.status(status).body(body);
	}

	private String newCorrelationId() {
		return UUID.randomUUID().toString();
	}
}
