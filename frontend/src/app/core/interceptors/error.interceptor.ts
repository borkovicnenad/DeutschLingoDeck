import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ApiErrorResponse, AppError } from '../../shared/models/api-error.model';

/** Converts backend HTTP errors into a standardized AppError shape. */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse)) {
        return throwError(() => error);
      }

      return throwError(() => toAppError(error));
    }),
  );
};

function toAppError(error: HttpErrorResponse): AppError {
  const body = error.error as Partial<ApiErrorResponse> | undefined;

  if (error.status === 0) {
    return { status: 0, code: 'NETWORK_ERROR', message: 'Unable to reach the server.' };
  }

  return {
    status: error.status,
    code: body?.code ?? 'UNKNOWN_ERROR',
    message: body?.message ?? 'An unexpected error occurred.',
    correlationId: body?.correlationId,
    fieldErrors: body?.errors,
  };
}
