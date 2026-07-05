/**
 * Authentication-specific exceptions (invalid credentials, invalid/expired
 * refresh token, duplicate email). Each extends a common.exception base type
 * so {@code GlobalExceptionHandler} maps it to the correct HTTP status
 * without needing its own handler method.
 */
package com.deutschlingodeck.authentication.exception;
