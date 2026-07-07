# Why DeutschLingoDeck Logged Out on Browser Refresh — Root Cause and Fix

The bug initially appeared to be a frontend authentication issue. The user successfully logged into the application, navigated to an authenticated page such as `/dashboard`, refreshed the browser, and was unexpectedly redirected to the login page. Since DeutschLingoDeck uses Angular together with JWT authentication, the first assumptions were that the frontend had lost the access token, Angular route guards were executing too early, or Server-Side Rendering (SSR) and hydration were interfering with authentication state.

After a systematic investigation, the actual cause turned out to be different. The logout was caused by an incorrect CORS configuration in Spring Security. Because preflight requests were rejected before the real API request could be sent, Angular never received the expected `401 Unauthorized` response and therefore never executed its refresh-token logic.

---

# 1. What Happened During Browser Refresh

Whenever the browser refreshes an authenticated page, Angular starts from scratch and attempts to restore the current session.

The application calls:

```text
GET /api/v1/auth/me
```

This endpoint verifies whether the currently stored access token still represents a valid authenticated user.

Since the frontend runs on:

```text
http://localhost:4200
```

and the backend runs on:

```text
http://localhost:8080
```

the browser treats this as a cross-origin request.

Before sending the actual authenticated request, the browser performs a CORS preflight request:

```text
OPTIONS /api/v1/auth/me
```

This request does not access any business functionality. It simply asks the backend whether the browser is allowed to perform the real request.

Unfortunately, Spring Security treated this OPTIONS request exactly like any other protected endpoint. Since preflight requests never contain a JWT access token, Spring Security immediately returned:

```text
401 Unauthorized
```

At that point the browser terminated the request before it even reached Angular.

As a consequence:

- Angular never received a normal HTTP 401 response.
- The refresh-token interceptor never executed.
- Session restoration failed.
- The authentication guard redirected the user to `/login`.

---

# 2. Why the Refresh Token Interceptor Did Not Work

The frontend already contained a refresh-token interceptor.

Its responsibility is straightforward:

```text
If an authenticated request returns HTTP 401:

    → call POST /auth/refresh

    → receive a new access token

    → retry the original request
```

However, this mechanism only works if Angular actually receives the HTTP response.

In this case Angular never received the backend's 401 response because the browser rejected the request during the CORS preflight phase.

Instead of receiving a `HttpErrorResponse`, Angular only saw a generic network failure (`net::ERR_FAILED`).

Therefore the interceptor never had an opportunity to refresh the access token.

---

# 3. Fixing Spring Security

The first backend modification was enabling CORS inside the Spring Security filter chain.

The following line was added:

```java
.cors(Customizer.withDefaults())
```

The security configuration now begins like this:

```java
http
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

This is important because configuring CORS only through `WebMvcConfigurer` is often insufficient once Spring Security becomes responsible for processing requests.

Spring Security must explicitly be instructed to apply CORS handling before authentication.

---

# 4. Allowing CORS Preflight Requests

The next change was allowing all HTTP OPTIONS requests without authentication.

The following matcher was added:

```java
.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
```

The authorization configuration therefore became:

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS).permitAll()
        .anyRequest().authenticated()
)
```

This is essential.

A CORS preflight request is not an authenticated business request. It only verifies whether the browser is allowed to send the actual request.

Requiring authentication for an OPTIONS request prevents every subsequent authenticated API request from ever reaching the backend.

---

# 5. Providing an Explicit CORS Configuration

The next addition was an explicit `CorsConfigurationSource` bean.

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(List.of("http://localhost:4200"));

    configuration.setAllowedMethods(List.of(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
    ));

    configuration.setAllowedHeaders(List.of("*"));

    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration("/**", configuration);

    return source;
}
```

This configuration tells Spring Security that requests originating from the Angular frontend are allowed to communicate with the backend while including credentials such as the `Authorization` header.

---

# 6. Correcting the OpenAPI Security Configuration

During testing another issue became apparent.

The application allowed:

```text
/v3/api-docs/**
```

but did not explicitly expose:

```text
/v3/api-docs
/v3/api-docs.yaml
```

As a result, requesting the YAML specification returned HTTP 401.

The public endpoint configuration was updated to include:

```java
"/v3/api-docs",
"/v3/api-docs/**",
"/v3/api-docs.yaml",
```

This allows both JSON and YAML OpenAPI specifications to be accessed without authentication.

---

# 7. Correcting the Angular Interceptor Order

The interceptor registration was also updated.

Originally:

```typescript
withInterceptors([
    authInterceptor,
    refreshTokenInterceptor,
    errorInterceptor
]);
```

was changed to:

```typescript
withInterceptors([
    authInterceptor,
    errorInterceptor,
    refreshTokenInterceptor
]);
```

Angular executes request interceptors in declaration order but processes responses in reverse order.

With the corrected order, `refreshTokenInterceptor` receives the original `HttpErrorResponse` before `errorInterceptor` converts it into an application-specific error object.

Although this was an important improvement, it was not the primary cause of the logout bug.

The real blocker was the failed CORS preflight request.

---

# 8. Temporary Token Lifetime Reduction

To reproduce the issue quickly during debugging, the access token lifetime was temporarily reduced.

Originally:

```yaml
access-token-ttl: PT15M
```

was changed to:

```yaml
access-token-ttl: PT10S
```

This caused access tokens to expire after only ten seconds, making it possible to reproduce the refresh scenario almost immediately.

Once testing is complete, the original lifetime should be restored.

---

# 9. A Remaining Improvement

During debugging an attempt was made to perform a manual refresh inside `restoreSession()`.

The implementation currently looks similar to:

```typescript
catchError(() => {

    this.refreshAccessToken().pipe(
        switchMap(() => this.authApi.me()),
        ...
    );

    this.clearSession();

    return of(false);
});
```

This code does not actually execute the refresh because the observable returned by `refreshAccessToken()` is never returned from `catchError()`.

The correct implementation should be:

```typescript
catchError(() => {
    return this.refreshAccessToken().pipe(
        switchMap(() => this.authApi.me()),
        tap(user => this.currentUserSignal.set(user)),
        map(() => true),
        catchError(() => {
            this.clearSession();
            return of(false);
        })
    );
});
```

If the interceptor-based refresh mechanism works correctly, this additional fallback may not even be necessary, but if it is kept, it should be implemented correctly.

---

# 10. Expected Network Flow After the Fix

After applying all fixes, refreshing the browser should produce the following sequence:

```text
OPTIONS /api/v1/auth/me
        │
        ▼
200 OK

        │
        ▼
GET /api/v1/auth/me
        │
        ▼
401 Unauthorized

        │
        ▼
POST /api/v1/auth/refresh
        │
        ▼
200 OK

        │
        ▼
GET /api/v1/auth/me
        │
        ▼
200 OK
```

The user remains on the same page without noticing that the access token expired.

Only when both the access token and the refresh token become invalid should the application clear the session and redirect the user back to the login page.

---

# Conclusion

Although the symptoms initially suggested an Angular authentication problem, the investigation showed that the logout-on-refresh bug originated in the backend.

Spring Security rejected CORS preflight requests with HTTP 401 before the browser could send the real authenticated request. Because the browser blocked the request, Angular never received the expected HTTP response and therefore never executed the refresh-token interceptor.

The issue was resolved by:

- enabling CORS inside Spring Security,
- allowing HTTP OPTIONS requests without authentication,
- providing an explicit `CorsConfigurationSource`,
- exposing all OpenAPI endpoints,
- correcting the Angular interceptor order.

This investigation demonstrates the importance of analysing the complete request lifecycle—from the browser, through CORS and Spring Security, to Angular interceptors—rather than assuming that authentication issues always originate in the frontend.