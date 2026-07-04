import { RenderMode, ServerRoute } from '@angular/ssr';

/**
 * Authenticated routes depend on a JWT stored in localStorage, which does not
 * exist during server-side rendering. They are therefore rendered client-side
 * only (RenderMode.Client). Public, data-free routes are safe to prerender.
 */
export const serverRoutes: ServerRoute[] = [
  { path: 'login', renderMode: RenderMode.Prerender },
  { path: 'register', renderMode: RenderMode.Prerender },
  { path: 'access-denied', renderMode: RenderMode.Prerender },
  { path: 'not-found', renderMode: RenderMode.Prerender },
  { path: '**', renderMode: RenderMode.Client },
];
