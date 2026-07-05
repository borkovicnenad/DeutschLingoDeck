package com.deutschlingodeck.authentication.exception;

import com.deutschlingodeck.common.exception.UnauthorizedException;

/**
 * Signals that a refresh token is malformed, expired, or has already been
 * revoked (via logout or a prior refresh, since refresh tokens rotate).
 */
public class InvalidRefreshTokenException extends UnauthorizedException {

	public InvalidRefreshTokenException() {
		super("Invalid or expired refresh token");
	}
}
