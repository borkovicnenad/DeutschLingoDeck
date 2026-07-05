package com.deutschlingodeck.authentication.exception;

import com.deutschlingodeck.common.exception.UnauthorizedException;

/**
 * Signals that a login attempt used an unknown email or a wrong password.
 * Deliberately carries the same message regardless of which of the two was
 * wrong, so responses do not reveal whether an email is registered.
 */
public class InvalidCredentialsException extends UnauthorizedException {

	public InvalidCredentialsException() {
		super("Invalid email or password");
	}
}
