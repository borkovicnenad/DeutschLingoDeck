import { ErrorHandler, Injectable } from '@angular/core';

/**
 * Centralized handler for unexpected (non-HTTP) errors so a single uncaught
 * exception never crashes the application. HTTP errors are normalized by the
 * Error Interceptor instead.
 */
@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  handleError(error: unknown): void {
    // TODO: forward to a remote logging/monitoring service.
    console.error('Unhandled application error:', error);
  }
}
