import { InjectionToken } from '@angular/core';

/**
 * Base URL of the DeutschLingoDeck REST API, e.g. http://localhost:8080/api/v1
 * Provided in app.config.ts from the environment configuration.
 */
export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL');
