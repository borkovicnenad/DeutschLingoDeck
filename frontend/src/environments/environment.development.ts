export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api/v1',
  /**
   * TEMPORARY DEV-ONLY BYPASS: when true, the app starts already
   * "logged in" as a fake local user instead of restoring a real session,
   * so the authenticated app can be exercised before backend auth exists.
   * Set back to false once /auth/login and /auth/register are ready.
   */
  bypassAuth: false,
};
