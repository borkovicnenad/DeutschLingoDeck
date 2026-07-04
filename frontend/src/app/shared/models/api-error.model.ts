import { FieldError } from './field-error.model';

/** Mirrors the backend ErrorResponse schema. */
export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  code: string;
  message: string;
  correlationId?: string;
  errors?: FieldError[];
}

/**
 * Normalized error used throughout the frontend, produced by the Error Interceptor
 * from a raw HttpErrorResponse so that components never depend on HTTP internals.
 */
export interface AppError {
  status: number;
  code: string;
  message: string;
  correlationId?: string;
  fieldErrors?: FieldError[];
}
