package com.deutschlingodeck.authentication.exception;

import com.deutschlingodeck.common.exception.ConflictException;

/** Signals that a registration attempt used an email that is already taken. */
public class EmailAlreadyExistsException extends ConflictException {

	public EmailAlreadyExistsException(String email) {
		super("Email '" + email + "' is already registered");
	}
}
