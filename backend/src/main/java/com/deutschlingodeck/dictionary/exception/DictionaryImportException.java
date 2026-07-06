package com.deutschlingodeck.dictionary.exception;

import com.deutschlingodeck.common.exception.ApplicationException;

/**
 * Signals that an uploaded dictionary file is fundamentally unprocessable
 * (wrong format, unreadable, or no usable rows). Maps to HTTP 422.
 */
public class DictionaryImportException extends ApplicationException {

	private static final String ERROR_CODE = "IMPORT_FAILED";

	public DictionaryImportException(String message) {
		super(message, ERROR_CODE);
	}
}
