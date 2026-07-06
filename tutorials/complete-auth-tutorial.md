# Spring Boot + Angular Authentication — A Deep Dive into DeutschLingoDeck

This is a from-first-principles walkthrough of the authentication system that actually exists in this repository (`com.deutschlingodeck.authentication`, `com.deutschlingodeck.security`, the Flyway migrations under `backend/src/main/resources/db/migration`, and the Angular code under `frontend/src/app/core/auth` + `core/interceptors` + `core/guards`).

Every mechanism explained here is grounded in a real class, method, table, or config key. Where a concept is generic (what is a JWT, what is BCrypt), the theory is introduced first and then immediately anchored to the concrete implementation in this codebase. By the end you should be able to rebuild this exact system — table schemas, entity classes, filters, interceptors, and all — without opening the source again.

## Table of Contents

1. [High-Level Architecture](#1-high-level-architecture)
2. [Complete Authentication Lifecycle](#2-complete-authentication-lifecycle)
3. [Frontend Perspective (Angular)](#3-frontend-perspective)
4. [HTTP Deep Dive](#4-http-deep-dive)
5. [JWT Deep Dive](#5-jwt-deep-dive)
6. [Access Token vs Refresh Token](#6-access-token-vs-refresh-token)
7. [Flyway and Database](#7-flyway-and-database)
8. [Spring Security Deep Dive](#8-spring-security-deep-dive)
9. [Authentication Service](#9-authentication-service)
10. [Repository Layer](#10-repository-layer)
11. [Security Scenarios](#11-security-scenarios)
12. [Security Best Practices](#12-security-best-practices)
13. [Sequence Diagrams](#13-sequence-diagrams)
14. [Code Walkthrough](#14-code-walkthrough)
15. [Architecture Review](#15-architecture-review)
16. [Final End-to-End Diagram](#16-final-end-to-end-diagram)

---

## 1. High-Level Architecture

### 1.1 Authentication vs Authorization

Authentication answers "who are you?" Authorization answers "what are you allowed to do?" They are different concerns that are easy to conflate because in most systems they are implemented back to back in the same request pipeline.

In this project:

- Authentication happens once, at `/api/v1/auth/login` or `/api/v1/auth/register`, when the `AuthenticationServiceImpl` verifies a password (login) or creates a new `User` row (register) and, in both cases, calls the private `issueTokens(User)` method to mint a JWT pair.
- Authorization happens on *every subsequent request*, inside `JwtAuthenticationFilter`, which reads the Authorization header, validates the JWT, and — if valid — places a Spring Security `Authentication` object into the `SecurityContextHolder`. Spring Security's `.anyRequest().authenticated()` rule (in `SecurityConfiguration`) then simply checks "is there an `Authentication` in the context?" to decide whether the request may proceed.

Notice something important about this codebase's authorization model: it is authentication-complete but authorization-simple. There are no roles, no `GrantedAuthority` values beyond an empty list (`List.of()` in `JwtAuthenticationFilter`), and no per-endpoint permission checks beyond "authenticated or not." That is a deliberate, honest reflection of the current feature set — the project has one authorization tier (logged in) and no concept of admin/user roles yet. Authorization as an *extension point* is present (Spring Security is fully wired), but the differentiated-permissions feature itself doesn't exist yet.

### 1.2 Why JWT?

A JSON Web Token (JWT) is a signed, self-describing credential. It bundles a set of claims ("who is this," "when was it issued," "when does it expire," "what kind of token is this") together with a cryptographic signature, all encoded into a single compact string of the form:

```
header.payload.signature
```

The critical property that makes JWTs attractive for an API backend like this one is that the server does not need to look anything up to validate the token's *authenticity and freshness* — it only needs the shared secret used to verify the signature. Compare this to a classic session cookie, where the server must query a session store (in memory, Redis, a DB table) on every request to find out who the session ID belongs to.

### 1.3 Why Stateless?

"Stateless" here specifically means: **the HTTP layer keeps no server-side session.** This project makes that decision explicit in `SecurityConfiguration`:

```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

Consequences:

- No `JSESSIONID` cookie is ever created by Spring Security.
- No memory (or Redis, or DB) is consumed per logged-in user for the *access* token's validity — the JWT itself carries everything necessary (`sub`, `email`, `displayName`, `type`, `exp`) to reconstruct the caller's identity, at the cost of a signature check.
- Any backend instance, in a load-balanced multi-instance deployment, can validate any token, because validation only requires the shared HMAC secret (`security.jwt.secret` in `application.yaml`), not shared session storage. This is why "stateless JWT" is a natural fit for horizontally scaled Spring Boot APIs — it trades a small amount of per-request CPU (HMAC verification) for the removal of a whole class of shared-state infrastructure.

### 1.4 Why Two Token Types?

If access tokens are stateless and self-validating, why introduce a second token (the refresh token) and a database table (`refresh_tokens`) at all? This is the central tension in stateless-JWT design:

- Stateless tokens cannot be revoked individually before they expire — the server has no record of having issued them, so there is nothing to delete.
- But real systems need revocation: a user must be able to log out, and a stolen token must eventually stop working.

The project's answer is the classic **two-tier token architecture**:

- The **access token** is short-lived (`security.jwt.access-token-ttl`, defaulting to `PT15M` — 15 minutes, ISO-8601 duration syntax) and carries the user's identity claims. It is stateless: the server never persists it, and validating it is pure cryptography (`JwtTokenProvider.parseToken`). If stolen, the blast radius is capped at 15 minutes.
- The **refresh token** is long-lived (`security.jwt.refresh-token-ttl`, defaulting to `P7D` — 7 days) but carries almost no information — only a `jti` (JWT ID) claim, a `sub` (subject/user id), and a `type` claim. Crucially, every refresh token's `jti` is mirrored into a **database row** (the `refresh_tokens` table, via the `RefreshToken` entity). That row is the single source of truth for whether the refresh token is still valid — the JWT itself is just an unforgeable *pointer* to that row.

This gives the system stateless authorization for the hot path (every regular API call only needs the access token, no DB hit for auth) while retaining statefulness exactly where revocation matters (refresh — a comparatively rare operation, happening a few times a day per user instead of on every request).

### 1.5 Advantages and Disadvantages of This Architecture

**Advantages:**
- No session storage infrastructure needed for the common case.
- Horizontally scalable: any backend instance can validate an access token.
- Fine-grained revocation is possible where it matters (refresh tokens), without paying the cost of persisting every access token.
- Stolen access tokens self-expire quickly (15 minutes).

**Disadvantages** (the project's own honest constraints, discussed further in Section 12 and Section 15):
- An access token **cannot** be revoked mid-flight. If a user's account is compromised or a security team needs to force-logout a specific device immediately, the current design can only revoke the *refresh* token (which stops new access tokens being minted) — the still-live access token continues to work for up to 15 more minutes. This is the classic and accepted tradeoff of stateless access tokens.
- The refresh endpoint does add one write + one read to the database on every token refresh (revoke old row, look up user), so it isn't "free" — but this cost is deliberately incurred only once every 15 minutes at most, not on every request.
- `JwtAuthenticationFilter` grants **no authorities** — every authenticated principal is equally privileged. This is fine for the current feature set (there's only one role) but would need extending (custom `GrantedAuthority` claims, or a DB lookup) before role-based authorization could exist.

### 1.6 Why This Architecture is Common in Spring Boot

Spring Security was built around the Servlet Filter chain and a pluggable `Authentication`/`SecurityContext` abstraction, which makes it straightforward to swap in a custom filter (`JwtAuthenticationFilter extends OncePerRequestFilter`) that populates the same `SecurityContextHolder` the rest of the framework already understands, without touching Spring MVC controllers or requiring parallel proprietary annotations. Once `SecurityContextHolder.getContext().setAuthentication(...)` is called, every other Spring Security feature (`@AuthenticationPrincipal`, `SecurityContextHolder.getContext().getAuthentication()` inside services, `.anyRequest().authenticated()`) works exactly as if a traditional username/password login had happened — the filter is a drop-in replacement for the authentication *mechanism*, while leaving the authorization *infrastructure* completely standard.

### 1.7 Architecture Diagram

```
+------------------------------------------------------------+
|                        Angular SPA                          |
|  AuthService  ->  AuthApiService  ->  HttpClient             |
|  TokenStorageService (localStorage)                          |
|  authInterceptor / refreshTokenInterceptor / errorInterceptor|
+---------------------------|---------------------------------+
                             | HTTPS
                             v
+------------------------------------------------------------+
|                     Spring Boot Backend                     |
|                                                              |
|   Servlet Filter Chain                                       |
|     -> JwtAuthenticationFilter (reads Bearer header)         |
|     -> UsernamePasswordAuthenticationFilter (unused path)    |
|     -> Spring Security authorization check                  |
|                                                              |
|   AuthenticationController  (/api/v1/auth/*)                 |
|     -> AuthenticationService / AuthenticationServiceImpl     |
|         -> PasswordEncoder (BCrypt)                          |
|         -> JwtTokenProvider (HS256 sign/verify)               |
|         -> UserRepository / RefreshTokenRepository (JPA)     |
|                                                              |
|   GlobalExceptionHandler (@RestControllerAdvice)              |
|   RestAuthenticationEntryPoint (401 JSON body)                |
+---------------------------|---------------------------------+
                             | JDBC
                             v
+------------------------------------------------------------+
|                        PostgreSQL                            |
|   users            (V1__create_users_table.sql)              |
|   refresh_tokens   (V2__create_refresh_tokens_table.sql)     |
|   flyway_schema_history (managed by Flyway itself)            |
+------------------------------------------------------------+
```

---

## 2. Complete Authentication Lifecycle

Below is the full path from a cold browser to an authenticated, working session, described stage by stage, quoting the actual classes involved.

### Stage 0 — App bootstrap (frontend)

On application start, `AuthService.restoreSession()` runs (typically wired into an app initializer). It has three branches:

1. `environment.bypassAuth === true` (dev-only escape hatch): sets a hardcoded `DEV_BYPASS_USER` signal value and resolves `true` immediately — no network call at all. This only exists in `environment.development.ts`; `environment.ts` (the production environment) hardcodes `bypassAuth: false`.
2. No access token in `TokenStorageService`: resolves `false` — user is anonymous, guarded routes redirect to `/login` via `authGuard`.
3. An access token exists: calls `GET /api/v1/auth/me`. If it succeeds, the `currentUserSignal` is populated from the response and `isAuthenticated()` becomes `true`. If it fails (expired token, network error), `clearSession()` wipes storage and the app falls back to anonymous.

### Stage 1 — Registration (first-time user)

```
Browser
  -> Angular RegisterPageComponent (not detailed here — data entry only)
  -> AuthService.register(request)
  -> AuthApiService.register(request)
  -> HTTP POST /api/v1/auth/register
       body: { "email": "...", "password": "...", "displayName": "..." }
  -> authInterceptor: sees /auth/register is in PUBLIC_ENDPOINTS, does NOT
     attach an Authorization header
  -> Spring Security Filter Chain
  -> JwtAuthenticationFilter runs on every request regardless, but there's
     no Bearer header so it's a no-op; SecurityContext stays empty
  -> SecurityConfiguration's authorizeHttpRequests rule permits this path
     (SecurityConstants.PUBLIC_ENDPOINTS includes "/api/v1/auth/register")
  -> AuthenticationController.register(@Valid @RequestBody RegisterRequest)
  -> Bean Validation runs first (@NotBlank @Email on email,
     @NotBlank @Size(min=12) on password, @NotBlank @Size(min=2,max=100)
     on displayName) — a validation failure short-circuits straight to
     GlobalExceptionHandler.handleMethodArgumentNotValid, never reaching
     the service.
  -> AuthenticationServiceImpl.register(request):
       - userRepository.existsByEmail(email) — if true, throws
         EmailAlreadyExistsException (-> HTTP 409, see Section 9 & 11)
       - passwordEncoder.encode(password) — BCrypt-hashes the raw password
       - constructs `new User(email, passwordHash, displayName)`,
         sets createdAt, saves via userRepository.save(user)
       - calls private issueTokens(user)
  -> issueTokens(user):
       - jwtTokenProvider.generateAccessToken(user)  -> signed JWT #1
       - tokenId = UUID.randomUUID()
       - jwtTokenProvider.generateRefreshToken(user, tokenId) -> signed JWT #2
       - expiresAt = now + refreshTokenTtl (7 days)
       - refreshTokenRepository.save(new RefreshToken(tokenId, user.getId(), expiresAt))
         — INSERT INTO refresh_tokens
       - returns new AuthResponse(accessToken, refreshToken, "Bearer",
         userMapper.toUserResponse(user))
  -> AuthenticationController wraps that in
     ResponseEntity.status(HttpStatus.CREATED) (201)
  -> Angular: AuthService.applyAuthResponse() stores both tokens in
     TokenStorageService (localStorage) and sets currentUserSignal
  -> Angular router typically navigates to the dashboard.
```

### Stage 2 — Login (returning user)

Nearly identical to registration but calling `AuthenticationServiceImpl.login(LoginRequest)`:

- `userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new)`
- `passwordEncoder.matches(rawPassword, user.getPasswordHash())` — BCrypt re-hashes the candidate password with the salt embedded in the stored hash and compares in constant time; if false, throws `InvalidCredentialsException` (-> HTTP 401)
- `issueTokens(user)` — same token minting as registration

Note the deliberate symmetry of the error: whether the email doesn't exist or the password is wrong, the exact same `InvalidCredentialsException("Invalid email or password")` is thrown. This is a security decision explained in Section 9 and 12 (no user enumeration).

### Stage 3 — Authenticated request (e.g. loading the dashboard)

```
Angular http call to, say, GET /api/v1/dictionaries
  -> authInterceptor: URL is not in PUBLIC_ENDPOINTS, accessToken exists
     in TokenStorageService -> clones request with header
     Authorization: Bearer <accessToken>
  -> Spring Security Filter Chain
  -> JwtAuthenticationFilter.doFilterInternal:
       - reads Authorization header, strips "Bearer " prefix
       - jwtTokenProvider.parseToken(token) — verifies HS256 signature
         and expiration in one call (throws JwtException if invalid/expired)
       - jwtTokenProvider.isAccessToken(claims) — checks the "type" claim
         equals "access" (rejects a refresh token used here by mistake)
       - userId = Long.valueOf(claims.getSubject())
       - SecurityContextHolder.getContext().setAuthentication(
           new UsernamePasswordAuthenticationToken(userId, null, List.of()))
  -> Spring Security's authorizeHttpRequests sees an Authentication object
     present -> `.anyRequest().authenticated()` passes
  -> DictionaryController -> ... -> DictionaryServiceImpl, which (if it
     needs the caller) reads the same SecurityContextHolder principal.
     (AuthenticationServiceImpl.currentUserId() shows exactly this
     pattern for /auth/me and /auth/logout.)
  -> Response flows back normally.
```

### Stage 4 — Access token expires (silent refresh)

```
Angular http call with an expired access token
  -> authInterceptor still attaches the (now expired) Bearer header —
     it doesn't know the token is expired, it just attaches whatever is
     in storage
  -> JwtAuthenticationFilter: jwtTokenProvider.parseToken(token) throws
     a JwtException (ExpiredJwtException specifically) because
     `.expiration(...)` is in the past -> caught, SecurityContextHolder
     is cleared -> filter chain continues with NO authentication set
  -> `.anyRequest().authenticated()` fails -> Spring Security's
     exceptionHandling triggers RestAuthenticationEntryPoint.commence()
     -> HTTP 401 with a JSON ErrorResponse body
  -> refreshTokenInterceptor (frontend) catches the 401, sees the failed
     URL is NOT one of AUTH_ENDPOINTS (so it's a real protected resource,
     not the login/refresh call itself)
  -> AuthService.refreshAccessToken():
       - reads refreshToken from TokenStorageService
       - AuthApiService.refresh({ refreshToken })
       - HTTP POST /api/v1/auth/refresh
  -> AuthenticationServiceImpl.refresh(RefreshTokenRequest):
       - parseRefreshTokenClaims(): jwtTokenProvider.parseToken() +
         isRefreshToken() check (rejects an access token used here)
       - tokenId = UUID.fromString(claims.getId()) — reads the `jti` claim
       - refreshTokenRepository.findByTokenId(tokenId)
            .filter(RefreshToken::isValid)   <- !revoked && not expired
            .orElseThrow(InvalidRefreshTokenException::new)
       - **rotation**: storedToken.setRevoked(true); save() — the
         presented refresh token is burned, single-use
       - userRepository.findById(storedToken.getUserId())
       - issueTokens(user) — a BRAND NEW access token AND a brand new
         refresh token (new tokenId, new DB row) are returned
  -> Angular applies the new AuthResponse (both tokens overwritten in
     localStorage)
  -> refreshTokenInterceptor retries the ORIGINAL failed request with the
     new access token attached
  -> If that retry also fails (refresh itself failed, e.g. refresh token
     also expired/revoked), AuthService.handleExpiredSession() is called:
     clears the session and navigates to /login.
```

### Stage 5 — Logout

```
Angular "Log out" button -> AuthService.logout()
  -> AuthApiService.logout() -> POST /api/v1/auth/logout (Bearer header
     attached by authInterceptor, since /auth/logout is NOT in that
     interceptor's PUBLIC_ENDPOINTS list)
  -> JwtAuthenticationFilter authenticates the request normally (the
     access token is still valid — logout doesn't require an already-
     expired token)
  -> AuthenticationController.logout() -> AuthenticationServiceImpl.logout()
  -> currentUserId() reads the SecurityContextHolder principal (set by
     the filter)
  -> refreshTokenRepository.revokeAllActiveForUser(userId) — a single bulk
     UPDATE that revokes **every** active refresh token belonging to that
     user (not just the one currently in use) — see Section 10 for why.
  -> HTTP 204 No Content
  -> Angular: regardless of success/failure (`catchError(() => of(void 0))`)
     it always clears local storage and navigates to /login — logging out
     locally must never be blocked by a network failure.
```

Note precisely what logout does NOT do: it does not blacklist the *current access token*. That token, if it hasn't expired yet, will continue to authenticate successfully against `JwtAuthenticationFilter` for up to its remaining TTL (at most 15 minutes) even after logout, because access token validation is purely cryptographic and stateless. This is the direct consequence of the architecture described in Section 1.4/1.5 and is discussed again in Section 11 and 12.

---

## 3. Frontend Perspective

### 3.1 Where tokens are stored, and why

`TokenStorageService` (`frontend/src/app/core/auth/token-storage.service.ts`) stores both the access and refresh token in **`localStorage`**, under the keys `dld_access_token` and `dld_refresh_token`. The class explicitly guards every read/write with `isPlatformBrowser(inject(PLATFORM_ID))` because Angular apps can be server-side-rendered, and `localStorage` doesn't exist during SSR — attempting to touch it there would throw a `ReferenceError`.

The class carries an explicit TODO acknowledging this is a placeholder:

> "Re-evaluate storage strategy before production (e.g. refresh token in an HttpOnly cookie) - localStorage is used here as a placeholder for the MVP."

This is worth understanding thoroughly because it is the single most consequential security tradeoff in the whole frontend.

**localStorage:**
- Persists across browser restarts/tabs, readable by any JavaScript running on the page's origin.
- Vulnerable to **XSS**: if an attacker manages to inject a script (e.g. through an unsanitized user-generated string rendered without escaping), that script can read `localStorage.getItem('dld_access_token')` and exfiltrate both tokens. There is no way to mark a localStorage entry as inaccessible to JavaScript.
- NOT automatically sent by the browser — must be manually attached (which is exactly what `authInterceptor` does), which conveniently also means localStorage tokens are naturally **immune to CSRF** (a forged cross-site form submission can't read localStorage and therefore can't forge the Authorization header).

**sessionStorage:**
- Same JS-readability/XSS exposure as localStorage, but scoped to a single tab and cleared when that tab closes. Not used here, but would trade "session survives browser restart" for a marginally smaller exposure window.

**Cookies (plain):**
- Automatically attached to same-origin (and depending on SameSite, cross-origin) requests by the browser itself — no interceptor code needed to attach them.
- Automatic attachment is exactly what makes plain cookies vulnerable to **CSRF**: a malicious site can trigger a request to your API and the browser will attach the cookie for you.

**HttpOnly cookies:**
- Set with the `HttpOnly` flag, which makes them **invisible to JavaScript** (`document.cookie` cannot read them). This closes the XSS token-theft vector completely — a compromised page can still make the browser send authenticated requests (via CSRF) but cannot exfiltrate the raw token to send to an attacker's own server.
- Requires CSRF protection to compensate (a SameSite=Strict/Lax attribute, and/or a CSRF token in a custom header for state-changing requests).

**Secure cookies:**
- The `Secure` flag ensures the cookie is only ever sent over HTTPS, preventing plain-text interception on the wire. Should always be paired with HttpOnly in production.

**SameSite:**
- `SameSite=Strict` — cookie never sent on cross-site navigation/requests. Strongest CSRF protection, but can break legitimate cross-site flows (e.g. a payment redirect back to your site).
- `SameSite=Lax` — cookie sent on top-level navigations (a user clicking a link) but not on cross-site subresource requests (images, fetch/XHR from another origin). Common default balance.
- `SameSite=None` — cookie sent everywhere; requires `Secure`. Needed for genuine third-party/cross-site embedding use cases.

**Which solution does THIS project use?**
localStorage, for **both** the access and the refresh token, is what `TokenStorageService` actually implements today. This is a pragmatic MVP choice that keeps the interceptor code simple (`Authorization: Bearer <token>` is easy to reason about and test) but leaves both tokens exposed to any successful XSS on the SPA.

**Which would be recommended for production?**
The typical hardened pattern — and the one the TODO comment gestures toward — is a **split strategy**:
- Access token: kept in memory (a plain JS variable / signal, never written to any Storage API) or still in localStorage if XSS risk is otherwise well mitigated (CSP, strict output encoding). Being short-lived (15 min) limits blast radius either way.
- Refresh token: moved into an **HttpOnly, Secure, SameSite=Strict/Lax cookie**, set directly by the backend's `Set-Cookie` response header rather than being returned in the JSON body at all. This means `AuthResponse.refreshToken()` would need to stop being serialized to the client and instead be written as a cookie by the controller, and `/auth/refresh` would need to read it from the incoming cookie rather than a request body field. That is a real backend change, not just a frontend one — it isn't implemented in this codebase today.

### 3.2 How the Authorization header is built

`authInterceptor` (`frontend/src/app/core/interceptors/auth.interceptor.ts`) is a functional `HttpInterceptorFn` (the modern Angular `provideHttpClient(withInterceptors([...]))` style, not the old class-based `HTTP_INTERCEPTORS` multi-provider). Its logic:

```typescript
const PUBLIC_ENDPOINTS = ['/auth/register', '/auth/login', '/auth/refresh'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStorage = inject(TokenStorageService);
  const isPublicEndpoint = PUBLIC_ENDPOINTS.some((e) => req.url.includes(e));
  const accessToken = tokenStorage.getAccessToken();

  if (isPublicEndpoint || !accessToken) {
    return next(req);
  }

  return next(req.clone({ setHeaders: { Authorization: `Bearer ${accessToken}` } }));
};
```

Two details worth internalizing:
- `HttpRequest` objects in Angular are **immutable**; you cannot mutate `req.headers` directly. `req.clone({ setHeaders: {...} })` is the idiomatic way to produce a modified copy that gets passed down the chain.
- Note `/auth/logout` and `/auth/me` are deliberately **absent** from `PUBLIC_ENDPOINTS` here — they DO get the Authorization header attached, because both require identifying the caller server-side (`/me` returns the current user; `/logout` revokes that user's tokens).

### 3.3 Automatic refresh and the interceptor pipeline

`refreshTokenInterceptor` (`refresh-token.interceptor.ts`) is a **second, separate** interceptor, composed after `authInterceptor` in the `withInterceptors([...])` array (interceptor order matters — request interceptors run in array order outbound, and the corresponding response handling in the reverse order as the response bubbles back up, much like middleware/onion layering).

Its job is purely reactive — it does not proactively check token expiry before sending a request; it waits for the backend to say 401 and only then acts:

```typescript
const AUTH_ENDPOINTS = ['/auth/register', '/auth/login', '/auth/refresh', '/auth/logout'];

return next(req).pipe(
  catchError((error) => {
    const isUnauthorized = error instanceof HttpErrorResponse && error.status === 401;
    if (!isUnauthorized || isAuthEndpoint) return throwError(() => error);

    return authService.refreshAccessToken().pipe(
      switchMap((accessToken) =>
        next(req.clone({ setHeaders: { Authorization: `Bearer ${accessToken}` } }))),
      catchError((refreshError) => {
        authService.handleExpiredSession();
        return throwError(() => refreshError);
      }),
    );
  }),
);
```

Why exclude `AUTH_ENDPOINTS` from triggering a refresh? Because a 401 from `/auth/login` means "wrong password," not "my access token expired" — trying to refresh in that case would be nonsensical (there may not even be a refresh token yet) and would mask the real error. Excluding `/auth/refresh` itself also prevents **infinite recursion**: if the refresh call itself returns 401 (refresh token expired/revoked), that failure must propagate directly to `handleExpiredSession()`, not trigger *another* refresh attempt.

The code candidly documents its own known limitation:

> "TODO: concurrent requests failing at the same time will each trigger their own refresh call. A shared in-flight refresh Observable should be introduced before production to guarantee refresh-token rotation is only triggered once per expiry."

This is a real and important gap given Stage 4 above: refresh tokens **rotate on every use** (the old one is revoked server-side). If the dashboard fires three parallel requests the instant the access token expires, all three get 401 near-simultaneously, and *all three* call `refreshAccessToken()` independently. The first refresh call succeeds and rotates the refresh token (revoking the original). The second and third calls, using the now-already-revoked (single-use) refresh token, will fail with `InvalidRefreshTokenException` — a real race condition possible with the current code, not merely hypothetical. The fix — sharing one in-flight refresh `Observable` across all callers (e.g. with `shareReplay(1)` gated by a `BehaviorSubject`) — is exactly what the TODO calls out.

### 3.4 How token expiration is detected

Notice this project does **not** proactively decode the JWT on the frontend to check `exp` before sending a request (there is no `jwt-decode` usage, no timer-based pre-emptive refresh). Expiration is discovered *reactively*, only when the backend actually rejects a request with 401. This is simpler to reason about (no clock-skew concerns between browser and server) at the cost of always paying for one failed round-trip per token expiry before the silent refresh kicks in.

### 3.5 How logout works from the frontend

`AuthService.logout()`:

```typescript
logout(): void {
  this.authApi.logout().pipe(catchError(() => of(void 0))).subscribe(() => {
    this.clearSession();
    this.router.navigateByUrl('/login');
  });
}
```

The `catchError(() => of(void 0))` is deliberate: even if the network call fails outright (offline, backend down), the `subscribe` callback still runs and clears the local session / navigates away. Logging a user out of their own browser must never depend on a successful round trip to the server — otherwise a network partition would trap the user in a logged-in UI state they can't escape.

### 3.6 Realistic JSON examples

Login request:

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "learner@example.com",
  "password": "correct horse battery staple"
}
```

Successful response (200):

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MiIsImVtYWlsIjoibGVhcm5lckBleGFtcGxlLmNvbSIsImRpc3BsYXlOYW1lIjoiSm9hbm5hIiwidHlwZSI6ImFjY2VzcyIsImlhdCI6MTc4ODUwMDAwMCwiZXhwIjoxNzg4NTAwOTAwfQ.signature",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI5ZjJjLi4uIiwic3ViIjoiNDIiLCJ0eXBlIjoicmVmcmVzaCIsImlhdCI6MTc4ODUwMDAwMCwiZXhwIjoxNzg5MTA0ODAwfQ.signature",
  "tokenType": "Bearer",
  "user": {
    "id": 42,
    "email": "learner@example.com",
    "displayName": "Joanna"
  }
}
```

Wrong password (401):

```json
{
  "timestamp": "2026-07-05T10:15:30.123Z",
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "Invalid email or password",
  "correlationId": "b3f1e6b0-...-...-...",
  "errors": []
}
```

---

## 4. HTTP Deep Dive

### 4.1 The protocol, briefly

HTTP is a text-based (in HTTP/1.1) request/response protocol over TCP (or, with HTTP/2+, a binary framed protocol over the same conceptual model). Every exchange is one **request** answered by one **response**.

A request has:
- A **request line**: `METHOD /path HTTP/1.1`, e.g. `POST /api/v1/auth/login HTTP/1.1`
- **Headers**: colon-separated key/value metadata, e.g. `Content-Type: application/json`, `Authorization: Bearer <token>`
- An optional **body**: for this API, always a JSON document on write operations (register/login/refresh)

A response has:
- A **status line**: `HTTP/1.1 200 OK`
- Headers (`Content-Type: application/json`, etc.)
- An optional body — this project's success responses are DTOs (`AuthResponse`, `UserResponse`) serialized to JSON by Jackson (via `JacksonConfig`); error responses are `ErrorResponse` records serialized the same way.

### 4.2 Content-Type and JSON serialization

Every `AuthenticationController` method is annotated `@RequestBody` on its DTO parameter — Spring MVC's `HttpMessageConverter` machinery (backed by Jackson) deserializes the incoming JSON body into, e.g., a `LoginRequest` **record** before validation even runs. Records are a natural fit here: they're immutable, have automatically-generated equals/hashCode/toString, and their canonical constructor is exactly what Jackson needs to populate from JSON field names (`email`, `password`) that match the record's component names.

`@Valid` triggers **Bean Validation** (Jakarta Validation, the JSR-380 successor) on that deserialized object, checking annotations like `@NotBlank`, `@Email`, `@Size(min = 12)`. If invalid, Spring throws `MethodArgumentNotValidException` *before* the controller method body ever executes — `GlobalExceptionHandler.handleMethodArgumentNotValid` catches it and returns 400 with a list of `FieldError`s.

### 4.3 Authorization header and Bearer tokens

`Authorization: Bearer <token>` is an HTTP-standard header (RFC 6750, "OAuth 2.0 Bearer Token Usage" — though this project doesn't implement full OAuth2, it reuses the same header convention because it's universally understood tooling-wise). "Bearer" literally means: whoever *bears* (presents) this token is assumed to be its rightful owner — there is no additional proof-of-possession step. This is why token secrecy (Section 3.1's storage discussion) matters so much: possession IS authentication.

`JwtAuthenticationFilter` parses this header manually:

```java
String header = request.getHeader(HttpHeaders.AUTHORIZATION);
if (header != null && header.startsWith("Bearer ")) {
    String token = header.substring("Bearer ".length());
    // ...
}
```

### 4.4 Status codes used, and why

| Status | Used for | Where |
|---|---|---|
| **200 OK** | Successful login, refresh, or `/auth/me` read | `ResponseEntity.ok(...)` |
| **201 Created** | Successful registration | `AuthenticationController.register` returns `ResponseEntity.status(HttpStatus.CREATED)` — correct because register creates a new `users` row (a new resource), unlike login which reads an existing one |
| **204 No Content** | Logout | `ResponseEntity.noContent().build()` — no meaningful body; frontend types it `Observable<void>` |
| **400 Bad Request** | Bean Validation failures (`MethodArgumentNotValidException`) and generic `BusinessException`/`ValidationException` subclasses | "the request itself is malformed or violates a business rule," distinct from 401/403 which are about identity/permission |
| **401 Unauthorized** | `InvalidCredentialsException` (wrong login), `InvalidRefreshTokenException` (bad/expired/revoked refresh token), `RestAuthenticationEntryPoint` (unauthenticated request to protected endpoint) | "who you are is not established/accepted" |
| **403 Forbidden** | `GlobalExceptionHandler.handleForbidden` for `ForbiddenException` | Not currently thrown anywhere in the authentication flow (no role/ownership check yet) but exists in the hierarchy for when authorization gets more granular |
| **404 Not Found** | `ResourceNotFoundException` | Not used inside authentication itself (an unknown email on login intentionally returns 401, not 404 — Section 12) but used elsewhere (e.g. dictionary/game lookups) |
| **409 Conflict** | `EmailAlreadyExistsException extends ConflictException` | Correct code for "the request is well-formed, but it collides with existing state" (a UNIQUE constraint on `users.email`, effectively) |

### 4.5 Complete request/response example — Registration

Request:

```http
POST /api/v1/auth/register HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "email": "new.learner@example.com",
  "password": "correct horse battery staple",
  "displayName": "New Learner"
}
```

Response:

```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "user": { "id": 101, "email": "new.learner@example.com", "displayName": "New Learner" }
}
```

Validation failure example (password too short):

```http
POST /api/v1/auth/register
{ "email": "x@example.com", "password": "short", "displayName": "X" }
```

```json
HTTP/1.1 400 Bad Request
{
  "timestamp": "2026-07-05T10:00:00.000Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "correlationId": "...",
  "errors": [
    { "field": "password", "message": "size must be between 12 and 2147483647" }
  ]
}
```

---

## 5. JWT Deep Dive

### 5.1 Structure, from first principles

A JWT is three Base64URL-encoded segments joined by dots:

```
header.payload.signature
```

**Header** — a small JSON object describing the token itself, e.g.:

```json
{ "alg": "HS256" }
```

**Payload** — the claims, a JSON object of arbitrary key/value pairs, some of which are registered/standard names with agreed meaning:

- `sub` (subject) — who this token is about. In this project, always the user's numeric `id`, stringified: `.subject(user.getId().toString())`.
- `exp` (expiration time) — a Unix timestamp (seconds since epoch) after which the token must be rejected. Set via `.expiration(Date.from(now.plus(ttl)))`.
- `iat` (issued at) — when the token was minted: `.issuedAt(Date.from(now))`.
- `jti` (JWT ID) — a unique identifier for this specific token instance. Used **only** on refresh tokens here: `.id(tokenId.toString())`, where `tokenId` is a fresh `UUID.randomUUID()`. This is the correlation key to the `refresh_tokens` database row (Section 6).
- Custom claims — this project adds `email`, `displayName` (access tokens only) and a custom `type` claim (`"access"` or `"refresh"`, both token types) that has no standard meaning in the JWT spec but is essential to *this* implementation's safety (Section 5.4).

**Signature** — computed over the header and payload (concatenated with a dot, both Base64URL-encoded) using the algorithm named in the header and a secret/key. Anyone possessing the same secret can *verify* the signature is authentic and that the header/payload haven't been tampered with, but cannot forge a new valid signature without that secret.

### 5.2 Decoding an example JWT

Take the access token fragment from Section 3.6:

```
eyJhbGciOiJIUzI1NiJ9
.
eyJzdWIiOiI0MiIsImVtYWlsIjoibGVhcm5lckBleGFtcGxlLmNvbSIsImRpc3BsYXlOYW1lIjoiSm9hbm5hIiwidHlwZSI6ImFjY2VzcyIsImlhdCI6MTc4ODUwMDAwMCwiZXhwIjoxNzg4NTAwOTAwfQ
.
<signature bytes>
```

Base64URL-decoding the header gives:

```json
{ "alg": "HS256" }
```

Base64URL-decoding the payload gives:

```json
{
  "sub": "42",
  "email": "learner@example.com",
  "displayName": "Joanna",
  "type": "access",
  "iat": 1788500000,
  "exp": 1788500900
}
```

`exp - iat = 900` seconds = 15 minutes, matching `security.jwt.access-token-ttl: PT15M` in `application.yaml`.

**Crucially: none of this is encrypted.** Base64 is an encoding, not encryption — anyone can decode a JWT's header and payload with zero secret knowledge (paste it into jwt.io and you'll see the claims in plaintext). Only the *signature* requires the secret. This is why JWTs must never carry sensitive secrets in claims (this project's claims — user id, email, display name — are all things the user's own client already knows, so this is safe here) and why the signature is what you're protecting, not the payload's confidentiality.

### 5.3 Signing, verification, HS256, symmetric cryptography

HS256 = **HMAC using SHA-256**. HMAC (Hash-based Message Authentication Code) is a *symmetric* scheme: the exact same secret key both signs and verifies. Contrast with RS256 (RSA signatures), an *asymmetric* scheme where a private key signs and a separate public key verifies — useful when the verifier (e.g. a different microservice or a third party) shouldn't be trusted with the ability to *mint* tokens, only to check them.

This project uses HS256 exclusively:

```java
this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
// ...
Jwts.builder()....signWith(key)....compact();       // signing
Jwts.parser().verifyWith(key).build().parseSignedClaims(token);  // verification
```

Because signing and verification use the *same* key, and both happen inside the same monolithic Spring Boot backend (`JwtTokenProvider` is the only class touching `key`), HS256 is the appropriate, simpler choice here — there's no need for asymmetric key distribution across independently-trusted services.

The secret itself comes from `JwtProperties`, a `@ConfigurationProperties` record bound to the `security.jwt.*` prefix:

```yaml
security:
  jwt:
    secret: ${JWT_SECRET:dev-only-secret-key-change-me-in-production-32bytes-min}
    access-token-ttl: ${JWT_ACCESS_TOKEN_TTL:PT15M}
    refresh-token-ttl: ${JWT_REFRESH_TOKEN_TTL:P7D}
```

The default value is explicitly named `dev-only-secret-key-change-me-in-production-32bytes-min` — both a working default for `docker-compose` and a loud reminder (32 bytes minimum is the practical floor for an HS256 key, matching SHA-256's 256-bit/32-byte block size) that production deployments MUST override it via the `JWT_SECRET` environment variable. If this secret ever leaks, an attacker can forge arbitrary access tokens for arbitrary user ids — this is the single most sensitive piece of configuration in the entire system.

### 5.4 Walking through JwtTokenProvider line by line

```java
@Component
public class JwtTokenProvider {
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final JwtProperties properties;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }
```

The key is derived once, in the constructor, from the configured secret string — `Keys.hmacShaKeyFor` (from the `io.jsonwebtoken` / JJWT library) wraps the raw bytes into a `javax.crypto.SecretKey` object, validating that it's long enough for the algorithm.

```java
public String generateAccessToken(User user) {
    Instant now = Instant.now();
    return Jwts.builder()
            .subject(user.getId().toString())
            .claim("email", user.getEmail())
            .claim("displayName", user.getDisplayName())
            .claim(CLAIM_TYPE, TYPE_ACCESS)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(properties.accessTokenTtl())))
            .signWith(key)
            .compact();
}
```

`Jwts.builder()` is JJWT's fluent builder. `.subject(...)` sets `sub`. `.claim(name, value)` adds an arbitrary custom claim. `.issuedAt`/`.expiration` set `iat`/`exp` as `Date` objects (JJWT converts these to Unix-epoch-seconds internally). `.signWith(key)` — because no algorithm is explicitly named here, JJWT infers HS256 from the `SecretKey`'s type/length. `.compact()` produces the final `header.payload.signature` string.

```java
public String generateRefreshToken(User user, UUID tokenId) {
    Instant now = Instant.now();
    return Jwts.builder()
            .id(tokenId.toString())
            .subject(user.getId().toString())
            .claim(CLAIM_TYPE, TYPE_REFRESH)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(properties.refreshTokenTtl())))
            .signWith(key)
            .compact();
}
```

Notice what's *missing* compared to the access token: no `email`, no `displayName`. The refresh token intentionally carries the bare minimum (`jti`, `sub`, `type`, `iat`, `exp`) because its only job is to prove "this is a still-valid, previously-issued refresh grant for user X" — the actual user data is re-fetched from `userRepository.findById(...)` inside `AuthenticationServiceImpl.refresh()` at refresh time, guaranteeing the returned `UserResponse` always reflects the *current* displayName/email even if the user changed their profile since the refresh token was issued (exactly parallel to the same reasoning documented on `JwtAuthenticationFilter`'s Javadoc for access tokens, Section 8.4).

```java
public Jws<Claims> parseToken(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
}
```

This single method does BOTH signature verification and expiration checking in one call. `.verifyWith(key)` configures the parser to check the HMAC signature; `parseSignedClaims(token)` throws `SignatureException` (bad signature — tampered or wrong secret), `ExpiredJwtException` (past `exp`), or `MalformedJwtException` (not well-formed JWT structure) — all subclasses of `io.jsonwebtoken.JwtException` — if anything is wrong. If it returns normally, the token is cryptographically authentic and currently unexpired.

```java
public boolean isAccessToken(Claims claims) {
    return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
}
public boolean isRefreshToken(Claims claims) {
    return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class));
}
```

This `type` claim is the answer to a real attack: **without it, an access token and a refresh token signed with the same secret would be indistinguishable to a verifier that only checks the signature.** A stolen *access* token (short-lived, no `jti`, so nothing to revoke) could otherwise be replayed against `/auth/refresh` — and since `UUID.fromString(claims.getId())` would then throw (access tokens have no `jti` at all — `claims.getId()` returns `null`, and `UUID.fromString(null)` throws `IllegalArgumentException`) it would actually already fail today even without the `type` check, but the explicit `isRefreshToken()` guard in `parseRefreshTokenClaims()` makes the rejection intentional and explicit rather than an accidental side effect of a null-handling crash — a much more robust and readable defense. Symmetrically, `isAccessToken()` in `JwtAuthenticationFilter` stops a stolen *refresh* token (which lacks `email`/`displayName` claims but otherwise has a valid signature) from being used directly as a Bearer access credential to call protected endpoints.

```java
public Duration getRefreshTokenTtl() {
    return properties.refreshTokenTtl();
}
```

Exposed so `AuthenticationServiceImpl.issueTokens()` can compute the `expires_at` column for the `refresh_tokens` table row without duplicating the TTL configuration value.

---

## 6. Access Token vs Refresh Token

### 6.1 Why two tokens, restated precisely

Short TTL (access) limits exposure from theft; long TTL (refresh) avoids forcing the user to log in with a password every 15 minutes. Splitting them lets each be tuned independently: `JWT_ACCESS_TOKEN_TTL` and `JWT_REFRESH_TOKEN_TTL` are separate environment variables.

### 6.2 Revocation and why only the refresh token is persisted

The `RefreshToken` entity's own Javadoc states this design decision explicitly:

> "The raw JWT itself is never stored - only this correlation row, so a refresh token can be revoked (logout, rotation) without keeping the signed token around."

Only the `jti` (`token_id` column, a `UUID`) is stored — never the actual signed JWT string. This is deliberate: storing raw JWTs in the database would be redundant (the `jti` is already an unforgeable, unique correlation key) and would create an unnecessary secondary copy of a bearer credential sitting in the database — if the DB were ever breached, storing raw tokens would leak live, directly-usable credentials rather than just an opaque UUID that's useless without also knowing the shared HMAC secret AND having a validly-signed JWT wrapper.

Access tokens are **never** persisted anywhere, by design — that is precisely what "stateless" means for them (Section 1.3). There is no way to revoke one mid-flight; you can only wait for it to expire (max 15 minutes) or revoke the *refresh* token so no *new* access token can be minted going forward.

### 6.3 Token rotation, implemented exactly

"Rotation" means: every time a refresh token is used, it is invalidated and replaced by a brand-new one (with a brand-new `jti`), never reused. This is implemented in `AuthenticationServiceImpl.refresh()`:

```java
RefreshToken storedToken = refreshTokenRepository.findByTokenId(tokenId)
        .filter(RefreshToken::isValid)
        .orElseThrow(InvalidRefreshTokenException::new);

// Rotate: the presented refresh token is single-use.
storedToken.setRevoked(true);
refreshTokenRepository.save(storedToken);

User user = userRepository.findById(storedToken.getUserId())
        .orElseThrow(InvalidRefreshTokenException::new);

return issueTokens(user);   // mints a NEW access token AND a NEW refresh token
```

Note this entire method is `@Transactional` — the revoke-write and the subsequent new-token-issue-and-save happen atomically; if anything later in the method throws, the revocation of the old token is rolled back too, so a failed refresh attempt can't leave the user's *only* refresh token burned with no replacement.

### 6.4 Why rotation defeats replay attacks

A **replay attack** here means: an attacker who has captured a refresh token in transit (e.g. via a compromised network, a logging mistake, XSS) tries to use it again later, possibly after the legitimate user has already used it. Without rotation, both the attacker and the legitimate user could use the *same* refresh token indefinitely until it naturally expires — the server has no way to distinguish the legitimate use from the replay.

With rotation:
- The legitimate user refreshes first -> their old refresh token is revoked, they receive a new one.
- The attacker later tries the *old* (now-revoked) token -> `.filter(RefreshToken::isValid)` fails (`revoked == true`) -> `InvalidRefreshTokenException` -> HTTP 401. The replay is rejected.
- **Detection bonus** (not currently implemented, but the data model supports it): if the *attacker* refreshes first with the stolen token, the *legitimate* user's next refresh attempt with what they think is their current token will also fail, because it's now the revoked one — this mismatch is a strong signal of token theft that a more advanced implementation could detect and respond to (e.g. by revoking the whole token family / forcing a re-login). This project does not implement that theft-detection response — it only implements the baseline rotation that prevents indefinite replay.

### 6.5 Why access tokens remain stateless despite all this

It would be *possible* to also persist every access token's `jti` and check it against a blocklist on every request — but that reintroduces exactly the per-request DB dependency that stateless JWTs exist to avoid, on the highest-frequency path in the whole system (every single authenticated API call, not just the occasional refresh). The design instead accepts a bounded, short window of non-revocability (max 15 minutes) as the cost of keeping the hot path stateless, and reserves the DB-backed revocation mechanism for the comparatively rare refresh operation.

### 6.6 Diagram — Token Rotation

```
Client                         Server                          DB
  |--- refreshToken A --------->|                               |
  |                             |-- findByTokenId(A).isValid? ->|  (true)
  |                             |-- storedToken(A).revoked=true |
  |                             |-- save(A) ------------------->|  UPDATE
  |                             |-- new tokenId B minted        |
  |                             |-- save(new RefreshToken B) -->|  INSERT
  |<-- access#2 + refreshToken B|                               |
  |                             |                               |

  [later: attacker replays refreshToken A]
  |--- refreshToken A --------->|                               |
  |                             |-- findByTokenId(A).isValid? ->|  (false: revoked)
  |<-- 401 InvalidRefreshToken -|                               |
```

---

## 7. Flyway and Database

### 7.1 Flyway architecture

Flyway is a database migration tool: instead of letting Hibernate auto-generate/alter tables from entity classes at runtime (fragile, unpredictable in production, impossible to code-review as a diff), you write explicit, numbered, immutable SQL scripts. Flyway tracks which scripts have already run, against which database, in a table it manages itself: **`flyway_schema_history`**. On every application startup, Flyway compares the migration files present on the classpath (`backend/src/main/resources/db/migration/V1__create_users_table.sql`, `V2__create_refresh_tokens_table.sql`, ...) against that history table, and applies — in strict version order — any migration not yet recorded as applied. Each row in `flyway_schema_history` records the version, description, checksum (so a migration file can't be silently edited after the fact without Flyway detecting the mismatch and refusing to proceed), execution time, and success flag.

The naming convention `V{version}__{description}.sql` is significant:
- `V1`, `V2` — strictly increasing version numbers Flyway sorts and applies in order.
- Double underscore `__` — required separator between version and description.
- `.sql` — plain SQL, run as-is (Flyway also supports Java-based migrations, not used here).

### 7.2 Why ddl-auto is disabled

`application.yaml` sets:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none
```

The comment right above it is precise about *why*, and about the current transitional state of the codebase:

> "Flyway now owns the schema for the User/RefreshToken entities (see db/migration). Remaining entities are still created via this 'none' setting until their own migrations are added."

This tells you two things: first, `ddl-auto: none` means Hibernate will **never** create, alter, or drop any table on its own — the schema is entirely Flyway's responsibility, which is the correct, production-safe setting (contrast with `update` or `create-drop`, which are convenient in a tutorial project but dangerous in any environment where accidental schema drift or data loss is unacceptable). Second, the comment is candid that this is a still-migrating codebase: only the auth-related tables currently have Flyway migrations; everything else is implied to still rely on some other mechanism (or hasn't been formalized yet) — a fact worth knowing rather than assuming the whole schema is Flyway-managed.

### 7.3 Migration execution on startup

Spring Boot's `spring-boot-starter-flyway` auto-configuration (implied by the presence of migration files and no explicit disabling) runs Flyway's `migrate()` automatically during application context startup, *before* the `EntityManagerFactory`/Hibernate session factory is fully initialized against the schema — this ordering guarantee is exactly why Flyway + `ddl-auto: none` is safe: by the time JPA starts issuing queries, the schema Flyway just created/updated is already in place and consistent with what the entity classes expect.

### 7.4 V1 — users table

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
);
```

- `id BIGSERIAL PRIMARY KEY` — a 64-bit auto-incrementing integer primary key. Matches the `User` entity's `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;` — `IDENTITY` tells Hibernate to rely on the database's own auto-increment mechanism (Postgres sequences backing `BIGSERIAL`) rather than a separate Hibernate-managed sequence table.
- `email VARCHAR(255) NOT NULL UNIQUE` — the natural login identifier. `UNIQUE` is what makes `userRepository.existsByEmail(...)` a meaningful guard — and it's also a safety net: even if application-level logic had a bug that let two registrations race past the `existsByEmail` check simultaneously, the database's unique constraint is the final backstop that guarantees no duplicate email ever gets persisted (the second insert would throw a `DataIntegrityViolationException` instead). Matches `@Column(nullable = false, unique = true) private String email;` on the entity.
- `password_hash VARCHAR(255) NOT NULL` — never the raw password (Section 12). 255 chars comfortably fits a BCrypt hash (which is always exactly 60 characters, `$2a$10$...`), leaving headroom if the hashing algorithm ever changes to something with a longer encoded output.
- `display_name VARCHAR(100) NOT NULL` — matches the DTO's `@Size(min = 2, max = 100)` constraint on `RegisterRequest.displayName`.
- `created_at TIMESTAMPTZ NOT NULL` — always set (`user.setCreatedAt(OffsetDateTime.now())` in `register()`), so it's safe to be `NOT NULL`. `TIMESTAMPTZ` (timestamp with time zone) is the correct Postgres type for any timestamp coming from `OffsetDateTime` — it stores an absolute instant rather than an ambiguous local wall-clock time, avoiding entire classes of timezone bugs.
- `updated_at TIMESTAMPTZ` (nullable) — only set when the user's profile is later modified (e.g. by `ProfileServiceImpl`, outside this tutorial's scope but visible in `User.setUpdatedAt`); a freshly-registered user naturally has no update yet, hence nullable.

### 7.5 V2 — refresh_tokens table

```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_id UUID NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
```

- `id BIGSERIAL PRIMARY KEY` — the table's own surrogate key, distinct from `token_id`. This is intentional and correct normalization: `id` is an internal row identifier never exposed anywhere; `token_id` is the externally-meaningful `jti` value. Keeping them separate means the internal primary key never needs to change shape even if, say, the JWT library's ID format changed.
- `token_id UUID NOT NULL UNIQUE` — the `jti` claim, mirrored from the JWT. `UNIQUE` guarantees `refreshTokenRepository.findByTokenId(tokenId)` (used in `refresh()`) can never ambiguously match more than one row — a load-bearing invariant, since `Optional<RefreshToken>` assumes at most one result. Being a `UUID` (128 bits of randomness from `UUID.randomUUID()`) makes it computationally infeasible to guess another user's `jti` by brute force.
- `user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE` — a foreign key tying every refresh token row to exactly one user. **`ON DELETE CASCADE`** means: if a `users` row is ever deleted (account deletion), every `refresh_tokens` row referencing it is **automatically deleted too**, by the database itself, with no application code needed. This prevents orphaned refresh-token rows from lingering forever pointless once their owning user no longer exists, and removes an entire class of "forgot to clean up related rows" bugs.
- `expires_at TIMESTAMPTZ NOT NULL` — mirrors the JWT's own `exp` claim, but as a first-class, independently-queryable database column. This duplication (the JWT already encodes its own expiry) is deliberate: it lets `RefreshToken.isValid()` check expiry with a simple in-memory comparison (`expiresAt.isAfter(OffsetDateTime.now())`) without needing to re-parse/re-verify the JWT again inside the entity, and — more importantly — it gives the database a durable, queryable fact ("when does this grant expire") independent of ever seeing the JWT string again.
- `revoked BOOLEAN NOT NULL DEFAULT FALSE` — the single field that turns a would-be-stateless refresh token into a revocable one. Flipped to `true` on rotation (`refresh()`) and in bulk on logout (`revokeAllActiveForUser`). Defaulting to `FALSE` means a freshly-inserted row (`new RefreshToken(tokenId, user.getId(), expiresAt)`, whose constructor also explicitly sets `this.revoked = false;`) is valid immediately.
- `created_at TIMESTAMPTZ NOT NULL DEFAULT now()` — an audit timestamp; `DEFAULT now()` means the database itself stamps the insert time even though the `RefreshToken` entity constructor also independently sets `this.createdAt = OffsetDateTime.now()` before the insert — belt and braces, though in practice the application-set value is what actually gets written since Hibernate sends an explicit value for a `NOT NULL` mapped column.
- **Index**: `idx_refresh_tokens_user_id` on `user_id`. This exists specifically to make `revokeAllActiveForUser(userId)` — an `UPDATE ... WHERE user_id = :userId AND revoked = false` — an efficient indexed lookup rather than a full table scan, which matters because that query runs on every logout and could otherwise degrade as the table accumulates rows over time (one row per login/refresh, essentially forever, since there's no cleanup job for expired/revoked rows in this codebase).

### 7.6 Relationships, summarized

```
users (1) ----< (many) refresh_tokens
   id  <----------------- user_id  (FK, ON DELETE CASCADE)
```

One user can have many refresh tokens simultaneously — this is precisely what enables "multiple devices" (Section 11): each login/registration/refresh call creates one *new* `refresh_tokens` row, so a user logged in on both a phone and a laptop simply has two independent, concurrently-valid rows.

---

## 8. Spring Security Deep Dive

### 8.1 The Servlet Filter Chain

Before a request ever reaches Spring MVC's `DispatcherServlet` (and therefore any `@RestController`), it passes through a chain of `javax.servlet.Filter` (Jakarta `jakarta.servlet.Filter`) instances, each able to inspect/modify the request, short-circuit it (write a response and stop), or pass it along via `filterChain.doFilter(request, response)`. Spring Security installs itself as (usually) a single `DelegatingFilterProxy`/`FilterChainProxy` early in this chain, internally delegating to an ordered list of its own filters — this project's custom `JwtAuthenticationFilter` is explicitly inserted into that internal list via:

```java
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

...meaning it runs **before** Spring Security's own default form-login filter (`UsernamePasswordAuthenticationFilter`, which this project doesn't actually use for any login form — the actual login happens through the `AuthenticationController` + `AuthenticationServiceImpl`, entirely bypassing that particular Spring Security filter — but its class reference is still a convenient, stable "before this canonical position" anchor).

### 8.2 OncePerRequestFilter

`JwtAuthenticationFilter extends OncePerRequestFilter`, a Spring-provided base class that guarantees `doFilterInternal` runs **exactly once per request**, even in environments with internal request forwarding/dispatching (e.g. error-page forwards) that could otherwise cause a naive `Filter` to run twice for the same logical request. This matters here because running the JWT-parsing logic twice would be wasteful and, worse, could interact strangely with `SecurityContextHolder` state if the second pass saw a context already set by the first.

### 8.3 Authentication, Authorization, Principal, GrantedAuthority

Spring Security's core abstractions:

- **`Authentication`** — an interface representing "the current request's identity claim," holding a `principal` (who), `credentials` (proof, often blanked out after authentication), and a collection of `GrantedAuthority` (what they're allowed to do — roles/permissions).
- **`Principal`** — the "who" object itself. Can be any type; commonly a `UserDetails` implementation, but this project uses something much simpler: a raw `Long` (the user id) — `new UsernamePasswordAuthenticationToken(userId, null, List.of())`.
- **`GrantedAuthority`** — this project passes `List.of()` (empty) as the authorities every single time. This is a direct, honest reflection of Section 1.1: there is exactly one authorization tier in this system today (authenticated vs not), so there are no roles to encode as authorities.
- **`SecurityContext`** — a small holder object wrapping the current `Authentication`.
- **`SecurityContextHolder`** — a static, `ThreadLocal`-backed (by default) registry giving any code, anywhere in the call stack of the current request-handling thread, access to the current `SecurityContext` without needing it passed as a parameter. This is exactly how `AuthenticationServiceImpl.currentUserId()` retrieves the caller's identity deep inside a service method, several layers away from the filter that originally set it:

```java
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
    throw new UnauthorizedException("Authentication is required");
}
return userId;
```

  Note the pattern-matching `instanceof Long userId` check doubles as a defensive assertion that the principal really is the `Long` type the filter is expected to have placed there — if somehow no authentication was set (e.g. `/auth/logout` called with no valid token — normally prevented by `.anyRequest().authenticated()`, but this is a legitimate defense-in-depth check), it throws `UnauthorizedException` rather than a `ClassCastException` or `NullPointerException`.

- **`AuthenticationEntryPoint`** — invoked by Spring Security precisely when an *unauthenticated* request is denied access to a protected resource (i.e., authentication itself is missing/failed, as opposed to authenticated-but-forbidden, which would go through an `AccessDeniedHandler` instead — not customized in this project, so it falls back to Spring Security's default, which returns 403 for a genuinely-authenticated-but-not-authorized request; this scenario doesn't currently arise since there's no role-based denial logic yet). `RestAuthenticationEntryPoint` is this project's implementation, replacing Spring Security's default (which would otherwise render an HTML error page — completely wrong for a JSON REST API) with a JSON `ErrorResponse` body.
- **`UsernamePasswordAuthenticationToken`** — a concrete, general-purpose `Authentication` implementation. Despite its name (which reflects its original purpose — form-based username/password login), it's simply a convenient off-the-shelf holder for `(principal, credentials, authorities)`, and this project reuses it purely as a data carrier for `(userId, null, emptyAuthorities)` — the "username/password" framing is vestigial; no actual password comparison happens at this point (that already happened back in `AuthenticationServiceImpl.login`).

### 8.4 JwtAuthenticationFilter, line by line

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                Claims claims = jwtTokenProvider.parseToken(token).getPayload();
                if (jwtTokenProvider.isAccessToken(claims)) {
                    Long userId = Long.valueOf(claims.getSubject());
                    var authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

Step by step:

1. Read the raw `Authorization` header. If absent, or it doesn't start with `"Bearer "`, the whole `if` block is skipped entirely — **the filter does nothing and calls `filterChain.doFilter` unconditionally at the end**. This is the single most important design property of this filter: **it never itself rejects a request.** It either populates the security context or it doesn't; the actual authorization *decision* (401 or pass) is made downstream by Spring Security's `.anyRequest().authenticated()` rule plus `RestAuthenticationEntryPoint`. This separation of concerns — "figure out who's asking" vs. "decide if that's good enough" — is exactly why public endpoints (login, register, refresh, docs, health) work fine even though this filter runs on literally every request, including those: there's simply no header to parse, so it's a silent no-op, and `SecurityConstants.PUBLIC_ENDPOINTS` permits the request regardless of whether any `Authentication` was set.
2. Strip the `"Bearer "` prefix to get the raw token string.
3. `jwtTokenProvider.parseToken(token).getPayload()` — verifies signature + expiry (Section 5.4) and extracts the `Claims` map. Any failure here throws immediately, caught by the surrounding `catch`.
4. `jwtTokenProvider.isAccessToken(claims)` — the `type` claim guard described in Section 5.4/6. **If this check fails (e.g. someone Bearer'd a refresh token), the `if` body is skipped silently — no exception, no context set — and the request proceeds unauthenticated**, which then gets rejected downstream by the standard `.anyRequest().authenticated()` + entry point path, exactly as if no token had been presented at all.
5. `Long.valueOf(claims.getSubject())` — parses the `sub` claim (a stringified user id) back into a `Long`.
6. Builds and installs the `Authentication` in the thread-local `SecurityContextHolder`.
7. The `catch (JwtException | IllegalArgumentException ex)` block handles both "the token library rejected this" (`JwtException` — bad signature, expired, malformed) and "the subject claim wasn't a valid number, or similar" (`IllegalArgumentException`, e.g. from `Long.valueOf` on garbage input) uniformly: **clear the context and move on.** Again, no exception is allowed to propagate out of the filter and abort the request — an invalid/expired token is treated exactly like "no token was given," deferring the actual 401 decision to the standard authorization check further down the chain. This is what makes Stage 4 of Section 2 (expired access token -> clean 401 -> frontend refresh flow) work smoothly, rather than the filter itself throwing an unhandled exception that would produce a generic 500.
8. `filterChain.doFilter(request, response)` — always called, unconditionally, as the last line. The request always continues down the chain; this filter only ever *adds* information, never blocks.

The class-level Javadoc explains the last remaining subtlety — why the principal is just a `Long` rather than the full user:

> "The authenticated principal is just the user's id (Long); services that need the full user re-read it from the database, which keeps displayName/email in the response always current instead of potentially stale for the lifetime of the access token."

If the filter instead embedded the full `email`/`displayName` directly into the `Authentication` principal (which it technically could, since those claims ARE present on the access token), any code relying on that principal would see whatever values were true at the moment the token was *issued* — up to 15 minutes stale. By storing only the immutable, never-changing `id`, and having services like `AuthenticationServiceImpl.getCurrentUser()` and `.logout()` re-query `userRepository.findById(userId)` fresh every time, the returned data is always current as of the actual request, not the token's mint time.

### 8.5 Request flow, end to end

```
Every HTTP request
  -> JwtAuthenticationFilter (always runs; sets or doesn't set SecurityContext)
  -> [other Spring Security internal filters]
  -> authorizeHttpRequests check:
        matches SecurityConstants.PUBLIC_ENDPOINTS? -> permit unconditionally
        else -> is there an Authentication in the context? -> permit
                else -> exceptionHandling -> RestAuthenticationEntryPoint -> 401
  -> DispatcherServlet -> @RestController method
  -> (inside the method/service) SecurityContextHolder.getContext()
     .getAuthentication().getPrincipal() -> Long userId, when needed
```

---

## 9. Authentication Service

`AuthenticationServiceImpl` is the single class implementing all business logic for the five `AuthenticationService` methods. It is annotated `@Service` (a semantic specialization of `@Component`, signaling "this is a business/service-layer bean" to both Spring's component scan and to human readers) and receives all five collaborators via constructor injection — `UserRepository`, `RefreshTokenRepository`, `PasswordEncoder`, `JwtTokenProvider`, `UserMapper` — with no field injection anywhere, which keeps the class trivially unit-testable (construct it directly with mocks, no Spring context required).

### 9.1 register()

```java
@Override
@Transactional
public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
        throw new EmailAlreadyExistsException(request.email());
    }

    User user = new User(
            request.email(),
            passwordEncoder.encode(request.password()),
            request.displayName());
    user.setCreatedAt(OffsetDateTime.now());
    user = userRepository.save(user);

    return issueTokens(user);
}
```

Database interactions: one `SELECT EXISTS(...)` (via `existsByEmail`), one `INSERT INTO users`, one `INSERT INTO refresh_tokens` (inside `issueTokens`). Security decision: the password is hashed with `passwordEncoder.encode(...)` — **never** stored or logged in plaintext at any point. Validation: Bean Validation on `RegisterRequest` already guaranteed a syntactically valid email, a password of at least 12 characters, and a display name between 2 and 100 characters before this method body even runs (Section 4.2). Exception: `EmailAlreadyExistsException` (409) if the email is already registered — checked *before* attempting the insert, though the database's own `UNIQUE` constraint (Section 7.4) is the authoritative backstop against any race condition between the check and the insert. `@Transactional` wraps the user insert and the refresh-token insert as one atomic unit — if token issuance somehow failed, the new user row would be rolled back too, avoiding a "user exists but received no tokens" inconsistent state.

### 9.2 login()

```java
@Override
public AuthResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.email())
            .orElseThrow(InvalidCredentialsException::new);

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
        throw new InvalidCredentialsException();
    }

    return issueTokens(user);
}
```

Database interactions: one `SELECT ... WHERE email = ?`, plus the same refresh-token `INSERT` inside `issueTokens`. **Not** annotated `@Transactional` — read-then-mint is naturally consistent without an explicit transaction boundary for the lookup itself (the write inside `issueTokens` still happens in its own auto-committed unit — see the note in 9.5 below about `issueTokens` not being independently transactional here, unlike in `register`/`refresh` where the enclosing method *is* `@Transactional`). Security decision: `passwordEncoder.matches(raw, hash)` — BCrypt re-derives the hash using the salt embedded in the stored value and compares digests, never by decrypting (BCrypt hashing is one-way; there is no "decrypt" back to the original password — Section 12 covers this in depth). Exception handling: both "email not found" and "password mismatch" throw the exact same `InvalidCredentialsException` — this uniformity is itself the security control (Section 12.1).

### 9.3 refresh()

Already walked through in full detail in Section 2 (Stage 4) and Section 6.3. To summarize its structure once more, concisely: parse and type-check the presented refresh JWT -> look up and validate the corresponding DB row -> rotate (revoke old, mint new) -> return a full new `AuthResponse`. Annotated `@Transactional` for the same atomicity reason as `register()`. Exception: `InvalidRefreshTokenException` (401) if the JWT fails to parse, isn't a refresh-type token, has no matching/valid DB row, or — implausibly, given the FK relationship, but defensively checked anyway — the owning user can no longer be found (`userRepository.findById(storedToken.getUserId()).orElseThrow(InvalidRefreshTokenException::new)`).

### 9.4 logout()

```java
@Override
@Transactional
public void logout() {
    refreshTokenRepository.revokeAllActiveForUser(currentUserId());
}
```

A single bulk `UPDATE` (Section 10) touching every currently-active refresh token for the calling user — not just whichever one happens to be in the request. Requires the caller to already be authenticated (`currentUserId()` reads the `SecurityContextHolder`, populated by `JwtAuthenticationFilter` from a still-valid access token — logging out does need a currently-valid access token, which makes sense: you must prove who you are in order to ask the server to revoke *your* sessions).

### 9.5 getCurrentUser()

```java
@Override
public UserResponse getCurrentUser() {
    User user = userRepository.findById(currentUserId())
            .orElseThrow(() -> new UnauthorizedException("Authentication is required"));
    return userMapper.toUserResponse(user);
}
```

Backs the `/auth/me` endpoint, used by the frontend's `restoreSession()` on app bootstrap (Section 2, Stage 0) to convert "I have an access token in storage" into "here's the actual current user object." Re-fetches the user row fresh from the database every single call — deliberately, per the `JwtAuthenticationFilter` Javadoc reasoning in Section 8.4 — rather than trusting the (possibly minutes-old) `email`/`displayName` claims embedded in the access token itself. Throws `UnauthorizedException` in the — extremely unlikely, since a valid access token was just used to reach this authenticated endpoint — case that the user row has since been deleted from under a still-valid token.

### 9.6 The shared helper: issueTokens()

```java
private AuthResponse issueTokens(User user) {
    String accessToken = jwtTokenProvider.generateAccessToken(user);

    UUID tokenId = UUID.randomUUID();
    String refreshToken = jwtTokenProvider.generateRefreshToken(user, tokenId);
    OffsetDateTime expiresAt = OffsetDateTime.now().plus(jwtTokenProvider.getRefreshTokenTtl());
    refreshTokenRepository.save(new RefreshToken(tokenId, user.getId(), expiresAt));

    return new AuthResponse(accessToken, refreshToken, "Bearer", userMapper.toUserResponse(user));
}
```

This one private method is the single point where every code path that ends in "the caller is now holding a fresh, valid token pair" converges — `register`, `login`, and `refresh` all call it, guaranteeing the token-minting + DB-row-creation logic can never drift out of sync between those three call sites (a classic DRY/single-responsibility win — if the token format ever changes, there's exactly one method to update).

---

## 10. Repository Layer

### 10.1 JpaRepository and Spring Data

Both `UserRepository` and `RefreshTokenRepository` extend `JpaRepository<Entity, IdType>`. This is Spring Data JPA's repository abstraction: by simply declaring an interface (no implementation class needed at all), Spring generates a full implementation at runtime providing `save`, `findById`, `findAll`, `deleteById`, and friends automatically. Method names following Spring Data's query-derivation convention (`findByEmail`, `existsByEmail`) are parsed by naming pattern and translated into the corresponding JPQL/SQL automatically — no manual query needed for either of those two.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

`findByEmail` returning `Optional<User>` is the idiomatic Spring Data pattern for "may or may not exist" lookups, forcing callers (`AuthenticationServiceImpl.login`) to explicitly handle the absent case (`.orElseThrow(...)`) rather than risking a silent `null`.

### 10.2 Custom queries: @Query and @Modifying

```java
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenId(UUID tokenId);

    @Modifying
    @Query("update RefreshToken t set t.revoked = true where t.userId = :userId and t.revoked = false")
    void revokeAllActiveForUser(@Param("userId") Long userId);
}
```

`findByTokenId` is another derived query, same mechanism as `findByEmail`.

`revokeAllActiveForUser` cannot be derived from a method name alone (Spring Data's naming convention has no vocabulary for "bulk update"), so it's hand-written as **JPQL** (Java Persistence Query Language — note it references `RefreshToken` the *entity* and `t.userId`/`t.revoked` the *entity fields*, not SQL table/column names — Hibernate translates this into the real SQL `UPDATE refresh_tokens SET revoked = true WHERE user_id = ? AND revoked = false` at runtime). `@Modifying` is **required** on any `@Query` that isn't a `SELECT` — it tells Spring Data this query mutates data and should be executed via `Query.executeUpdate()` rather than treated as a result-set query, and it also signals that the current Hibernate persistence-context cache should be treated carefully around this operation (bulk updates bypass the entity cache, so any already-loaded `RefreshToken` entities in the current session won't automatically reflect the change without a refresh — not a concern in this codebase's usage pattern, since nothing holds a stale in-memory `RefreshToken` across this call).

### 10.3 Why revokeAllActiveForUser() revokes ALL active tokens, not one

`logout()` calls this with no reference to which specific device/session is logging out — it revokes **every** currently-active (`revoked = false`) refresh token belonging to that user, across however many devices/browsers they're logged into. This is a deliberate simplification: the current `/auth/logout` endpoint has no concept of "log out just this device" (which would require the client to identify *which* refresh token/session it holds, e.g. by passing its own `jti` or a session identifier), so it implements the coarser, safer default of "log out everywhere." This is documented behavior worth knowing precisely because it directly affects the "multiple devices" scenario in Section 11: logging out on your phone will also silently invalidate your laptop's refresh token (though the laptop's *current* access token keeps working for its remaining TTL, per Section 1.5/6.5).

### 10.4 Transactions

`revokeAllActiveForUser` is invoked from `AuthenticationServiceImpl.logout()`, itself annotated `@Transactional`. Bulk `@Modifying` queries in Spring Data JPA **require** being run within a transaction (either the repository method's own, if separately annotated, or — as here — an enclosing `@Transactional` service method); without one, Spring Data throws an `InvalidDataAccessApiUsageException` at runtime. The transaction here is trivial (a single statement) but the annotation is what makes the operation valid at all, and it's what would allow additional logout-time side effects (e.g. an audit log insert) to be added later while preserving atomicity.

---

## 11. Security Scenarios

**Successful registration**
`RegisterRequest` validated (Bean Validation) -> `AuthenticationController.register` -> `AuthenticationServiceImpl.register` -> `existsByEmail`=false -> BCrypt encode -> `User` saved -> `issueTokens` -> `RefreshToken` saved -> 201 + `AuthResponse`.
Classes involved: `AuthenticationController`, `RegisterRequest`, `User`, `UserRepository`, `PasswordEncoder`, `JwtTokenProvider`, `RefreshToken`, `RefreshTokenRepository`, `UserMapper`, `AuthResponse`.

**Successful login**
`LoginRequest` validated -> `AuthenticationController.login` -> `AuthenticationServiceImpl.login` -> `findByEmail` found -> `matches()`=true -> `issueTokens` -> 200 + `AuthResponse`.
Classes involved: same set minus `User`'s constructor path (existing user fetched, not created), plus `InvalidCredentialsException` class (not thrown, but on the classpath of what *could* happen).

**Wrong password**
`findByEmail` found -> `matches(raw, hash)` = false -> throw `InvalidCredentialsException` -> `GlobalExceptionHandler.handleUnauthorized` -> 401 `{ code: "UNAUTHORIZED", message: "Invalid email or password" }`.
Classes: `InvalidCredentialsException`, `GlobalExceptionHandler`, `ErrorResponse`.

**Unknown email**
`findByEmail` -> `Optional.empty()` -> `.orElseThrow(InvalidCredentialsException::new)` -> identical 401 response body/shape as "wrong password." Deliberately indistinguishable from the outside (Section 12.1) — no separate "email not found" code path or message exists in the actual code.

**Expired access token**
`JwtAuthenticationFilter.parseToken` throws `ExpiredJwtException` (a `JwtException` subtype) -> caught -> `SecurityContextHolder.clearContext()` -> request proceeds unauthenticated -> `.anyRequest().authenticated()` fails -> `RestAuthenticationEntryPoint.commence()` -> 401. Frontend: `refreshTokenInterceptor` catches it, calls `AuthService.refreshAccessToken()`, retries transparently (Section 2 Stage 4, Section 3.3).
Classes: `JwtAuthenticationFilter`, `JwtTokenProvider`, `RestAuthenticationEntryPoint`, (frontend) `refreshTokenInterceptor`, `AuthService`, `AuthApiService`.

**Expired refresh token**
`AuthenticationServiceImpl.refresh` -> `parseRefreshTokenClaims` -> `jwtTokenProvider.parseToken` throws `ExpiredJwtException` -> caught in `parseRefreshTokenClaims`'s try/catch -> re-thrown as `InvalidRefreshTokenException` -> 401. Frontend: `refreshTokenInterceptor`'s inner `catchError` sees the refresh call itself failed -> `AuthService.handleExpiredSession()` -> `clearSession()` + navigate to `/login`.
Classes: `AuthenticationServiceImpl`, `JwtTokenProvider`, `InvalidRefreshTokenException`, `GlobalExceptionHandler`, (frontend) `refreshTokenInterceptor`, `AuthService`.

**Revoked refresh token**
Same code path as "expired," but the failure is `.filter(RefreshToken::isValid)` returning empty because `revoked == true` rather than because `expiresAt` has passed — both collapse to the same `InvalidRefreshTokenException` -> 401. This is the exact mechanism that defeats a replayed, previously-rotated-away refresh token (Section 6.4) and the exact mechanism that makes a post-logout refresh attempt fail (since `logout()` sets `revoked = true` on every active token for that user).
Classes: `RefreshToken.isValid()`, `RefreshTokenRepository`, `AuthenticationServiceImpl`.

**Display name change**
(Handled by `ProfileServiceImpl`, outside the authentication package, but directly relevant to why the `JwtAuthenticationFilter` Javadoc insists on re-fetching fresh data.) After a display name update, the user's *current, already-issued* access token still contains the OLD displayName claim (JWTs are immutable once signed) — but since neither `getCurrentUser()` nor `logout()` nor any other authenticated endpoint in this project trusts that claim for anything (they only use `sub` to look up the user id, then re-query the database), the change is reflected correctly on the very next `/auth/me` call, with zero need to reissue tokens. Only if some future code started trusting the `displayName` claim directly from the JWT would this become stale until the next token refresh.

**Logout**
Authenticated request (valid access token required) -> `currentUserId()` -> `refreshTokenRepository.revokeAllActiveForUser(userId)` -> bulk `UPDATE` -> 204. Frontend clears local storage and navigates to `/login` unconditionally (Section 3.5), independent of whether the network call even succeeded.
Classes: `AuthenticationController`, `AuthenticationServiceImpl`, `RefreshTokenRepository`, (frontend) `AuthService`, `TokenStorageService`.

**Multiple devices**
Each successful login/registration/refresh creates its own independent `refresh_tokens` row (own `token_id`, own `expires_at`) tied to the same `user_id` — the schema (Section 7.5) places no uniqueness constraint on `user_id` alone, only on `token_id`. So a phone and a laptop session coexist as two separate rows. As noted in Section 10.3, `logout()` on either device revokes BOTH (and every other) active row for that user — there is no per-device-scoped logout in this implementation.

**Token theft**
An attacker who obtains a copy of a valid access token (e.g. via XSS reading localStorage, Section 3.1) can use it exactly like the legitimate user for up to its remaining TTL (at most 15 minutes) — there is no fingerprinting (IP address, user-agent, device binding) tying a token to a specific client in this implementation, so the server cannot distinguish the thief's requests from the legitimate user's. If the attacker also obtains the refresh token, they can keep minting fresh access tokens indefinitely (each valid another 15 minutes) until either token is revoked. The legitimate user's only recourse, once theft is suspected, is to call `/auth/logout` (revoking all refresh tokens for the account) — but any access token the attacker already holds from before that point keeps working until it naturally expires; this is the architecture's core limitation (Section 1.5, 6.5) and there is no additional theft-specific mitigation (like device fingerprinting or a forced-invalidate-all-access-tokens mechanism) implemented here.

**Replay attack**
Covered exhaustively in Section 6.4 — token rotation on refresh is the specific, implemented defense. Replaying an old (already-rotated) refresh token fails with `InvalidRefreshTokenException` because `RefreshToken.isValid()` returns false (`revoked == true`) for it.

**Database unavailable**
Any repository call (`existsByEmail`, `findByEmail`, `save`, `findById`, the bulk update) would throw a Spring Data Access exception (e.g. `DataAccessResourceFailureException`, wrapping the underlying JDBC connectivity failure). None of these are caught anywhere in `AuthenticationServiceImpl` or the authentication-specific exception classes — they propagate up and are caught by `GlobalExceptionHandler.handleUnexpected(Exception ex)`, the catch-all `@ExceptionHandler(Exception.class)` at the bottom of the handler, which logs the error server-side with a fresh correlation ID and returns a generic 500 `INTERNAL_ERROR` body — deliberately **not** leaking any database-specific detail to the client, while still allowing the server-side log line (tagged with that same correlation ID) to be cross-referenced for debugging.

---

## 12. Security Best Practices

**Password hashing / BCrypt / Salt** — IMPLEMENTED. `SecurityConfiguration` exposes `new BCryptPasswordEncoder()` as the `PasswordEncoder` bean, used for both `.encode()` (register) and `.matches()` (login). BCrypt automatically generates and embeds a random salt into its output hash string on every `.encode()` call (that's why hashing the same password twice produces two different-looking hash strings) — this defeats precomputed rainbow-table attacks, since an attacker can't precompute hashes for common passwords without knowing each user's individual salt in advance. BCrypt is also intentionally slow (tunable work factor, defaulting to strength 10 in Spring Security's `BCryptPasswordEncoder`), which throttles brute-force guessing even if a hash database were ever leaked.

**JWT Secret / environment variables** — IMPLEMENTED, with an explicit loud dev-only default. `security.jwt.secret: ${JWT_SECRET:dev-only-secret-key-change-me-in-production-32bytes-min}` — the `${VAR:default}` Spring placeholder syntax means any real deployment MUST set `JWT_SECRET` as an actual environment variable/secret; the fallback is intentionally named to be impossible to miss in a code review or accidental production deploy.

**HTTPS** — NOT enforced anywhere in this codebase (no HTTPS redirect filter, no `Secure`-flagged cookies since no cookies are used at all, no HSTS header configuration visible in `SecurityConfiguration`). This is a deployment-environment concern (typically handled by a reverse proxy/load balancer terminating TLS in front of the Spring Boot app) rather than something coded into the application itself — worth flagging as a **future improvement**: without HTTPS in front of this API, both Bearer tokens and login credentials would travel in cleartext over the network.

**Token expiration** — IMPLEMENTED. Both TTLs are configured and enforced (Sections 5, 6); access tokens are deliberately short (15 min default), refresh tokens moderate (7 days default).

**Session fixation** — NOT APPLICABLE / inherently avoided. Session fixation attacks exploit servers that let a client set/reuse a pre-known session identifier across the authentication boundary. Since this API is fully stateless (`SessionCreationPolicy.STATELESS`, Section 1.3) and issues a brand new, cryptographically random token pair on every successful login/register/refresh, there is no server-side session identifier to fixate in the first place.

**Replay attacks** — MITIGATED for refresh tokens via rotation (Section 6.4). NOT mitigated for access tokens beyond their short TTL — a captured, still-valid access token can be replayed by an attacker until it naturally expires; there's no nonce/single-use mechanism on access tokens (nor would there typically be, since that would reintroduce per-request state).

**Timing attacks** — PARTIALLY MITIGATED. `passwordEncoder.matches(...)` (BCrypt) performs its internal hash comparison in a manner resistant to timing side-channels by design (constant-time-ish comparison as part of the BCrypt algorithm implementation in Spring Security). However, the *email lookup* (`findByEmail`) happening (or not) before the password check does introduce a small, theoretically measurable timing difference between "email exists, wrong password" (does a full BCrypt comparison) and "email doesn't exist" (throws immediately, no BCrypt computation at all) — a sufficiently sophisticated attacker measuring response-time distributions at scale could, in principle, infer whether an email is registered, even though the *response body* is identical in both cases. This is a known, common, generally-accepted-as-low-risk residual gap in this exact pattern (also present in many real-world systems) — a stricter mitigation would run a dummy/constant-cost BCrypt comparison even on the "email not found" path to equalize timing, which this codebase does not do.

**Information leakage / user enumeration** — WELL MITIGATED for login. `InvalidCredentialsException`'s own Javadoc states the intent directly:

> "Deliberately carries the same message regardless of which of the two was wrong, so responses do not reveal whether an email is registered."

Both "unknown email" and "wrong password" produce byte-for-byte identical 401 response bodies. NOTE, however: **registration is NOT symmetric** — `EmailAlreadyExistsException` explicitly returns 409 with the message `"Email '<email>' is already registered"`, which DOES confirm whether a given email already has an account. This is an intentional, common tradeoff (registration UX generally needs to tell a user "you already have an account, try logging in instead") but is worth naming explicitly as a narrower, deliberate enumeration surface that exists specifically on the registration endpoint, not the login endpoint.

**Brute force attacks / rate limiting** — NOT IMPLEMENTED. There is no rate limiter, no account lockout after repeated failed logins, no CAPTCHA, anywhere in this codebase. `AuthenticationServiceImpl.login` will happily process unlimited attempts from the same client. This is a clear, real **future improvement** — e.g. a request-rate limiter (in-memory token bucket, or Redis-backed for a multi-instance deployment) keyed by IP+email, and/or a progressive lockout/backoff after N consecutive failures for a given account.

**CSRF** — Explicitly disabled (`.csrf(csrf -> csrf.disable())` in `SecurityConfiguration`) and this is actually CORRECT for the current architecture, not an oversight: CSRF protection exists to stop a malicious site from making the browser automatically send credentials it holds (i.e., cookies) to your API. Since this API's tokens live in `localStorage` and are attached **manually** by `authInterceptor` (never automatically by the browser), a third-party page has no way to make the victim's browser attach the Authorization header on its behalf — the CSRF threat model simply doesn't apply to a header-based, non-cookie bearer scheme. (This would flip back to being a real requirement if the recommended HttpOnly-cookie refresh-token migration from Section 3.1 were implemented — that's precisely the tradeoff a cookie-based refresh token would reintroduce.)

**XSS** — NOT specifically mitigated by the authentication code itself (Angular's default template sanitization provides broad, framework-level XSS protection app-wide, but that's orthogonal to anything in the auth module). Because both tokens live in plain `localStorage` (Section 3.1), a successful XSS anywhere in the SPA can exfiltrate **both** the access and refresh tokens. This is the single largest concrete security gap this tutorial has surfaced, and the codebase itself acknowledges it via the `TokenStorageService` TODO. **Future improvement**: HttpOnly cookie for the refresh token at minimum (Section 3.1), plus a strict Content-Security-Policy header to reduce the odds of a successful script injection in the first place.

**Secure cookies** — NOT APPLICABLE currently (no cookies are used). Becomes directly relevant the moment the HttpOnly-refresh-token migration above is implemented.

Summary table:

| Practice | Status | Where |
|---|---|---|
| Password hashing (BCrypt) | Implemented | `SecurityConfiguration`, `AuthenticationServiceImpl` |
| Salting | Implemented (BCrypt built-in) | — |
| JWT secret via env var | Implemented | `application.yaml`, `JwtProperties` |
| Token expiration | Implemented | `JwtTokenProvider`, `application.yaml` |
| Refresh token rotation | Implemented | `AuthenticationServiceImpl.refresh` |
| Refresh token revocation | Implemented | `RefreshTokenRepository`, `RefreshToken.isValid` |
| User enumeration (login) | Implemented | `InvalidCredentialsException` |
| User enumeration (register) | NOT mitigated (409 reveals existing email) | by design tradeoff |
| Session fixation | N/A (stateless) | — |
| CSRF protection | N/A (no cookies; correctly disabled) | — |
| HTTPS enforcement | NOT implemented | deployment-layer responsibility |
| Rate limiting / lockout | NOT implemented | future improvement |
| Timing-attack hardening | Partial (BCrypt yes; email-lookup short-circuit no) | — |
| XSS-resistant token storage | NOT implemented | localStorage for both tokens |
| Secure/HttpOnly cookies | N/A | no cookies used |

---

## 13. Sequence Diagrams

### Registration

```
Browser        Angular          Backend Filter        Controller/Service        DB
  |--fill form-->|                                                                |
  |               |--POST /auth/register-------------->|                         |
  |               |    (no Authorization header;        |                         |
  |               |     authInterceptor skips it)        |                         |
  |               |                       JwtAuthFilter: no header, no-op          |
  |               |                       PUBLIC_ENDPOINTS permits it              |
  |               |                                      |--@Valid RegisterRequest|
  |               |                                      |--existsByEmail-------->|
  |               |                                      |<--false----------------|
  |               |                                      |--BCrypt.encode(pwd)    |
  |               |                                      |--save(User)----------->|
  |               |                                      |<--User(id=101)---------|
  |               |                                      |--issueTokens           |
  |               |                                      |--save(RefreshToken)--->|
  |               |                                      |<--saved----------------|
  |               |<--201 AuthResponse-------------------|                         |
  |               |--store tokens, set currentUser        |                         |
  |<--navigate to dashboard--|                            |                         |
```

### Login

```
Browser        Angular          Backend                                    DB
  |--credentials->|--POST /auth/login------------------->|                          |
  |               |                                       |--findByEmail----------->|
  |               |                                       |<--User-----------------|
  |               |                                       |--BCrypt.matches?        |
  |               |                                       |--issueTokens/save------>|
  |               |<--200 AuthResponse--------------------|                          |
  |               |--store tokens                          |                          |
```

### Authenticated request

```
Angular              authInterceptor         JwtAuthFilter              Controller
  |--GET /dictionaries-->|                                                          |
  |                       |--attach Bearer------->|                                 |
  |                       |                         |--parseToken (verify+expiry)   |
  |                       |                         |--isAccessToken? yes            |
  |                       |                         |--set SecurityContext(userId)   |
  |                       |                         |--.anyRequest().authenticated() OK
  |                       |                         |------------------------------->|
  |                       |                         |<--200 data----------------------|
  |<--200 data------------|                         |                                |
```

### Refresh

```
Angular                          Backend                                     DB
  |--401 on protected call-->|                                                     |
  | (refreshTokenInterceptor catches)                                              |
  |--POST /auth/refresh {refreshToken}--------->|                                  |
  |                                               |--parse+isRefreshToken check    |
  |                                               |--findByTokenId(jti)----------->|
  |                                               |<--RefreshToken row-------------|
  |                                               |--.isValid()? true               |
  |                                               |--revoked=true; save()--------->|
  |                                               |--findById(userId)------------->|
  |                                               |<--User-------------------------|
  |                                               |--issueTokens: NEW pair          |
  |                                               |--save(new RefreshToken)-------->|
  |<--200 new AuthResponse-----------------------|                                  |
  |--store new tokens                             |                                  |
  |--retry original request with new access token------------------------------->   |
```

### Logout

```
Angular                Backend                                          DB
  |--POST /auth/logout (Bearer)-->|                                          |
  |                                 |--JwtAuthFilter authenticates normally  |
  |                                 |--currentUserId()                       |
  |                                 |--revokeAllActiveForUser(userId)------->|  UPDATE ... SET revoked=true
  |<--204 No Content----------------|                                          |
  |--clearSession(); navigate /login (always, even on network failure)         |
```

### Expired access token

```
Angular                    JwtAuthFilter              EntryPoint
  |--GET /protected (expired Bearer)-->|                          |
  |                                     |--parseToken throws       |
  |                                     |   ExpiredJwtException    |
  |                                     |--clearContext()          |
  |                                     |--filterChain continues,  |
  |                                     |  unauthenticated          |
  |                                     |------------------------->|
  |<--401 ErrorResponse-----------------|                          |
  | (refreshTokenInterceptor takes over from here -> "Refresh" diagram above)
```

### Expired refresh token

```
Angular                                Backend
  |--POST /auth/refresh {expired refreshToken}-->|
  |                                                |--parseToken throws ExpiredJwtException
  |                                                |--caught -> InvalidRefreshTokenException
  |<--401 ErrorResponse---------------------------|
  |--handleExpiredSession(): clearSession(); navigate to /login
```

---

## 14. Code Walkthrough

**AuthenticationController**
- Responsibility: HTTP-layer adapter — maps five REST operations (register/login/refresh/logout/me) onto `AuthenticationService` calls and picks the correct `ResponseEntity` status per operation (201/200/200/204/200).
- Collaborators: `AuthenticationService` (interface, injected via constructor — depends on the abstraction, not `AuthenticationServiceImpl` directly).
- Lifecycle: a singleton Spring bean (default scope), stateless itself — every field is `final` and set once at construction.
- Important methods: all five endpoint methods; each is a thin pass-through with zero business logic of its own.
- Why it exists: keeps HTTP concerns (status codes, `@RequestBody`/`@Valid`, routing) fully separate from business logic, so the service layer can be unit-tested with zero servlet/HTTP machinery involved.
- SOLID: Single Responsibility (HTTP mapping only); Dependency Inversion (depends on the `AuthenticationService` interface).
- Annotations: `@RestController` (combines `@Controller` + `@ResponseBody` — every method's return value is serialized directly as the response body, not resolved as a view name); `@RequestMapping(ApiConstants.API_BASE_PATH + "/auth")` (class-level path prefix, `/api/v1/auth`); `@PostMapping`/`@GetMapping` (HTTP-method-specific routing); `@Valid` (triggers Bean Validation on the request body before the method executes); `@RequestBody` (deserializes JSON into the DTO record).

**AuthenticationService / AuthenticationServiceImpl**
- Responsibility: all authentication business logic (Section 9).
- Collaborators: `UserRepository`, `RefreshTokenRepository`, `PasswordEncoder`, `JwtTokenProvider`, `UserMapper`.
- Lifecycle: singleton bean; stateless aside from its injected, themselves-stateless collaborators.
- Why the interface exists separately from the implementation: enables substituting a test double or an alternate implementation without touching `AuthenticationController`, and documents the service's public contract independent of its internals — a classic Dependency Inversion / Interface Segregation pattern, common (if sometimes debated as boilerplate for a single-implementation case) in enterprise Spring Boot codebases.
- SOLID: Single Responsibility (auth logic only, no HTTP/persistence plumbing of its own); Dependency Inversion (all five collaborators injected as interfaces/abstractions, not concrete classes reached into directly).
- Annotations: `@Service`; `@Transactional` on `register`, `refresh`, `logout` (the three methods with multi-step writes that must be atomic); deliberately absent from `login` and `getCurrentUser` (read-mostly paths where the only "write" — token issuance inside `issueTokens` during login — is a single insert, tolerable outside an explicit transaction boundary given the codebase's own convention here).

**User (entity)**
- Responsibility: JPA-mapped representation of a row in the `users` table; the aggregate root of a person's identity in this system.
- Collaborators: none directly — pure data holder, referenced by `UserRepository`, `UserMapper`, and every service that needs identity data.
- Lifecycle: managed by the JPA persistence context per-transaction; `protected User()` no-arg constructor exists solely for Hibernate's reflective instantiation, while the public constructor is the only one application code is meant to use, enforcing that every `User` created by business logic starts with email/passwordHash/displayName populated.
- Annotations: `@Entity`, `@Table(name = "users")`, `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`, `@Column(nullable = false, unique = true)` on email.

**RefreshToken (entity)**
- Responsibility: the persisted correlation row for one issued refresh token (Section 6.2, 7.5). The `isValid()` method is the single piece of actual logic on this otherwise-plain data class: `!revoked && expiresAt.isAfter(OffsetDateTime.now())`.
- Collaborators: none directly; referenced by `RefreshTokenRepository` and `AuthenticationServiceImpl`.
- Why it exists: to make refresh tokens revocable despite the JWTs themselves being stateless/immutable once signed (Section 6.2).
- SOLID: Single Responsibility — this class knows only "is this grant currently valid," nothing about JWT parsing/signing, which stays entirely in `JwtTokenProvider`.

**UserRepository / RefreshTokenRepository**
- Responsibility: persistence access for their respective entities (Section 10).
- Collaborators: Spring Data JPA's runtime-generated proxy implementation; ultimately, the JDBC/Hibernate stack talking to PostgreSQL.
- Why interfaces with no implementation class: Spring Data JPA's whole value proposition — declare intent, get an implementation for free.
- Annotations: none needed on the interfaces themselves beyond `@Modifying`/`@Query`/`@Param` on the one custom method; `extends JpaRepository<T, ID>` is what triggers Spring Data's component-scanning and proxy generation.

**JwtTokenProvider**
- Responsibility: the single source of truth for minting and validating both token types (Section 5.4). No other class in the codebase touches `Jwts.builder()`/`Jwts.parser()` directly.
- Collaborators: `JwtProperties` (configuration), `User` (entity, read-only, to source claim values), the JJWT library (`io.jsonwebtoken.*`).
- Lifecycle: singleton; the `SecretKey` is derived once at construction and reused for the bean's whole lifetime (deriving it per-call would be wasteful and pointless, since the underlying secret string never changes at runtime).
- SOLID: Single Responsibility (JWT cryptography and claim shape, nothing else); this centralization is exactly what let Section 5.4 explain the `type` claim's security purpose as a single, auditable piece of logic rather than duplicated validation scattered across callers.
- Annotations: `@Component`.

**JwtAuthenticationFilter**
- Responsibility: bridge between the stateless JWT world and Spring Security's `SecurityContextHolder` world, on every single request (Section 8.4).
- Collaborators: `JwtTokenProvider` (all parsing/validation delegated to it — the filter itself contains zero cryptography).
- Lifecycle: singleton `@Component`, but its `doFilterInternal` logic is inherently per-request (state is only ever read from/written to the per-request `HttpServletRequest`/thread-local `SecurityContextHolder`, never held on the bean itself — safe to be a singleton).
- Why it exists: the sole integration point letting a completely custom, non-Spring-Security-native credential format (a Bearer JWT) plug into the framework's standard authorization pipeline.
- SOLID: Single Responsibility (authenticate from a header, nothing about deciding *whether* authentication is required — that's `SecurityConfiguration`'s job).
- Annotations: `@Component` (so it can be constructor-injected into `SecurityConfiguration`); extends `OncePerRequestFilter` (Section 8.2).

**JwtProperties**
- Responsibility: strongly-typed, immutable binding of the `security.jwt.*` YAML keys.
- Why a record: records are naturally immutable and give free equals/hashCode/toString — ideal for a pure configuration-value holder with no behavior.
- Annotations: `@ConfigurationProperties(prefix = "security.jwt")`, activated via `@EnableConfigurationProperties(JwtProperties.class)` on `SecurityConfiguration`.

**RestAuthenticationEntryPoint**
- Responsibility: replace Spring Security's default HTML 401 error page with a JSON `ErrorResponse`, matching the rest of the API's error contract (Section 8.3).
- Collaborators: `JsonMapper` (Jackson) to serialize the body; `ErrorResponse`.
- Annotations: `@Component`; implements `AuthenticationEntryPoint` (Spring Security's SPI for this exact extension point).

**SecurityConfiguration**
- Responsibility: wires the whole security posture together — filter ordering, session policy, CSRF, endpoint authorization rules, and the `PasswordEncoder` bean (Section 8, Section 1.3).
- Collaborators: `RestAuthenticationEntryPoint`, `JwtAuthenticationFilter`, `SecurityConstants`.
- Annotations: `@Configuration`, `@EnableWebSecurity`, `@EnableConfigurationProperties(JwtProperties.class)`; `@Bean` on both `filterChain` and `passwordEncoder`.

**SecurityConstants**
- Responsibility: the single, centralized list of endpoints that bypass authentication (Section 8.5's authorization check). A `final` class with a private constructor and only `static final` members — the standard Java idiom for a pure constants holder, guaranteeing it can never be instantiated.

**GlobalExceptionHandler**
- Responsibility: translates every exception type this codebase defines (plus the framework's `MethodArgumentNotValidException` and a catch-all `Exception`) into the single, consistent `ErrorResponse` JSON shape (Section 4.4, 11's "database unavailable" scenario).
- Collaborators: `ErrorResponse`, `FieldError`, `ValidationErrorExtractor`.
- Annotations: `@RestControllerAdvice` (a `@ControllerAdvice` + `@ResponseBody` combination, applying globally across every `@RestController` in the application — including `AuthenticationController` — without any per-controller wiring); `@ExceptionHandler(SpecificException.class)` per method.

**Exception hierarchy** (`ApplicationException` -> `ConflictException`, `UnauthorizedException`, `ForbiddenException`, `ResourceNotFoundException`, `BusinessException`, `ValidationException`; authentication-specific: `EmailAlreadyExistsException extends ConflictException`, `InvalidCredentialsException extends UnauthorizedException`, `InvalidRefreshTokenException extends UnauthorizedException`)
- Responsibility: each concrete leaf class carries both a human message and a stable machine-readable `errorCode` (`ApplicationException.getErrorCode()`), letting `GlobalExceptionHandler` map broad categories (all `ConflictException`s -> 409, all `UnauthorizedException`s -> 401) generically, while still preserving a specific, meaningful code/message per concrete exception type.
- Why this shape: adding a new domain-specific 401 case (say, a future "AccountLockedException") only requires extending `UnauthorizedException` — no change needed to `GlobalExceptionHandler` at all, since `handleUnauthorized(UnauthorizedException ex)` already handles the whole subtree via Spring's exception-handler resolution (which matches the most specific handler for the actual thrown type, walking up the class hierarchy).
- SOLID: Open/Closed — the hierarchy is open to new leaf exceptions, closed to modification of the handler dispatch logic itself.

**DTOs** (`RegisterRequest`, `LoginRequest`, `RefreshTokenRequest`, `AuthResponse`, `UserResponse`)
- Responsibility: define the exact wire shape of every request/response body, decoupled from the `User`/`RefreshToken` entity's internal shape (notably: `UserResponse` never includes `passwordHash` — the entity-to-DTO boundary is exactly where that sensitive field gets dropped).
- Why records: immutability, free equals/hashCode/toString, and constructor-based validation-annotation placement (`record RegisterRequest(@NotBlank @Email String email, ...)`) is the idiomatic modern-Java way to define these.

**UserMapper**
- Responsibility: `User` entity -> `UserResponse` DTO conversion — a single method, `toUserResponse`.
- Why MapStruct (`@Mapper(config = MapStructConfig.class)`): rather than hand-writing `new UserResponse(user.getId(), user.getEmail(), user.getDisplayName())` everywhere (or worse, having it drift out of sync as fields are added/removed), MapStruct generates the mapping implementation at compile time by matching field names, giving compile-time-checked, reflection-free, high-performance mapping with essentially zero boilerplate maintenance burden.

---

## 15. Architecture Review

**Strengths**
- Clean separation of concerns across every layer: HTTP (`AuthenticationController`) -> business logic (`AuthenticationServiceImpl`) -> persistence (`UserRepository`/`RefreshTokenRepository`) -> cryptography (`JwtTokenProvider`) -> Spring Security integration (`JwtAuthenticationFilter`/`SecurityConfiguration`). Each class has a narrow, correctly-scoped responsibility, and the codebase is candid (via Javadoc and inline comments) about *why* nontrivial decisions were made — a genuinely rare and valuable trait that makes onboarding and review far easier than average.
- The token-rotation + DB-correlation-row design (Section 6) is a textbook-correct, production-grade implementation of revocable refresh tokens over otherwise-stateless JWTs — this is not a toy simplification; it's the same pattern used by many real-world OAuth2/OIDC providers.
- Flyway-managed schema with `ddl-auto: none` (Section 7) is the professionally-correct choice for schema evolution — reviewable, versioned, reproducible.
- The symmetric `InvalidCredentialsException` (Section 9.2, 12) is a specific, deliberate, well-executed defense against user enumeration on login — many real systems get this wrong.
- MapStruct-based DTO mapping keeps entity internals (`passwordHash`) from ever leaking into a response, structurally (there is no path by which `UserResponse` could accidentally include it, since the DTO record simply has no such field).

**Weaknesses**
- Zero role/permission model: every authenticated principal is equally privileged (`List.of()` authorities, Section 8.3). This isn't wrong for the current feature set, but it means the *authorization* half of the system (as opposed to authentication) is essentially unbuilt — adding any admin-only or ownership-scoped endpoint in the future will require real design work that doesn't yet have a foothold in this code (no `GrantedAuthority` population, no method-level `@PreAuthorize` usage visible in the authentication module).
- No rate limiting / brute-force protection (Section 12) — a real gap for a publicly-reachable login endpoint.
- Token storage strategy (`localStorage` for both tokens, Section 3.1/12) is the weakest link in the whole system from a pure security-exposure standpoint, and the codebase's own TODOs already flag it as an accepted MVP tradeoff, not an oversight.
- No detection/response mechanism for refresh-token theft beyond basic rotation (Section 6.4) — a stolen-then-used-by-attacker-first scenario isn't specially flagged or responded to (e.g. no forced full-account token invalidation triggered automatically on detecting reuse of an already-rotated token).
- `refreshTokenInterceptor`'s documented concurrent-refresh race condition (Section 3.3) is a genuine, reachable bug under real usage patterns (multiple simultaneous API calls after token expiry), not just a theoretical edge case.

**Maintainability** — High. Small, single-purpose classes; consistent naming and package structure (`authentication.controller/service/repository/entity/dto/exception/mapper`); a well-factored common exception hierarchy that scales to new features without touching shared infrastructure.

**Scalability** — Good for the stateless access-token hot path (any instance validates any token with zero shared state); refresh operations do require the shared PostgreSQL instance, but that's an infrequent, low-volume operation per user, so it shouldn't become a bottleneck at any realistic scale without other, unrelated database load being the actual constraint.

**Performance** — BCrypt is intentionally slow (a *feature*, not a performance bug, for password hashing); JWT verification is fast, pure CPU (HMAC), no I/O. The one avoidable inefficiency worth naming: `RefreshToken` rows are never cleaned up — expired and revoked rows accumulate in the table forever (no scheduled deletion job visible anywhere in the codebase), which will eventually make that table (and its index) grow unbounded relative to active users. A periodic cleanup job (delete rows where `expires_at < now() - retention_window`) would be a straightforward, low-risk improvement.

**Security** — Solid fundamentals (hashing, rotation, revocation, enumeration-resistant login) undermined by two concrete, known gaps: XSS-exposed token storage and absent rate limiting. Both are explicitly acknowledged, not hidden, in the code itself.

**Clean Architecture / SOLID** — The authentication package broadly respects Single Responsibility and Dependency Inversion (interfaces for `AuthenticationService`, `UserMapper`; repositories as abstractions). Open/Closed is well demonstrated by the exception hierarchy. There's no strict "ports and adapters"/hexagonal layering enforced (e.g. the service directly imports JPA entities rather than a persistence-agnostic domain model), which is a pragmatic, common choice for a Spring Boot CRUD-shaped service rather than a violation — full hexagonal architecture would be over-engineering for this feature's actual complexity.

**Spring Boot best practices** — Constructor injection throughout (no field `@Autowired` anywhere shown), `@ConfigurationProperties` records instead of scattered `@Value` injections, externalized secrets via environment variables with safe dev defaults, Flyway over `ddl-auto`, and a centralized `@RestControllerAdvice` — this all reads as an experienced, idiomatic Spring Boot codebase, not a tutorial-grade one.

**Comparison to common enterprise Spring Boot authentication systems**
- Compared to systems built directly on Spring Security's `UserDetailsService` + `AuthenticationManager` + `DaoAuthenticationProvider` chain, this codebase takes a **simpler, more direct** path: it doesn't implement `UserDetailsService` at all — `AuthenticationServiceImpl.login` just does its own `findByEmail` + `passwordEncoder.matches` directly, bypassing Spring Security's `AuthenticationManager` abstraction entirely for the login step (Spring Security's authentication machinery is only engaged for *authorization* of subsequent requests, via the custom filter, not for the login call itself). This is a legitimate, common simplification for a purely stateless-JWT REST API where there's no `UsernamePasswordAuthenticationFilter`-driven form login to integrate with — many real projects make exactly this same choice.
- Compared to systems using Spring Authorization Server or a third-party IdP (Okta, Auth0, Keycloak) for full OAuth2/OIDC compliance, this is a deliberately narrower, self-rolled JWT scheme — no scopes, no OIDC discovery, no standardized token introspection endpoint, no refresh-token grant negotiated per RFC 6749. That's an entirely appropriate choice for a single first-party SPA talking to a single first-party API (the problem OAuth2/OIDC's extra complexity exists to solve — third-party clients, delegated authorization, SSO across multiple relying parties — doesn't exist here), but it's worth knowing precisely what's *not* implemented if the system ever needs to support third-party API clients or SSO in the future.

---

## 16. Final End-to-End Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                                BROWSER                                   │
│                                                                            │
│   ┌───────────────────────────── ANGULAR ─────────────────────────────┐  │
│   │                                                                     │  │
│   │  AuthService (signals: currentUser, isAuthenticated)                │  │
│   │     login()/register()/logout()/restoreSession()/refreshAccessToken()│ │
│   │            │                              ▲                         │  │
│   │            ▼                              │                         │  │
│   │  AuthApiService ──HTTP──▶  authInterceptor ──▶ refreshTokenInterceptor│ │
│   │       (adds Authorization:   (catches 401,        ──▶ errorInterceptor│ │
│   │        Bearer <token> unless   calls refreshAccessToken,               │  │
│   │        public endpoint)        retries once)                          │  │
│   │            │                                                          │  │
│   │  TokenStorageService (localStorage: dld_access_token, dld_refresh_token)│ │
│   └────────────────────────────│──────────────────────────────────────────┘ │
└────────────────────────────────│────────────────────────────────────────────┘
                                   │ HTTPS (Authorization: Bearer <accessToken>)
                                   ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                          SPRING BOOT BACKEND                               │
│                                                                             │
│   Servlet Filter Chain                                                     │
│   ┌─────────────────────────────────────────────────────────────────────┐ │
│   │ JwtAuthenticationFilter (OncePerRequestFilter)                       │ │
│   │   read Authorization header → JwtTokenProvider.parseToken (verify)  │ │
│   │   isAccessToken? → SecurityContextHolder.setAuthentication(userId)  │ │
│   │   on JwtException/IllegalArgumentException → clearContext(), no-op │ │
│   │   ALWAYS filterChain.doFilter() — never blocks the request itself   │ │
│   └─────────────────────────────────────────────────────────────────────┘ │
│                        │                                                   │
│   SecurityConfiguration: PUBLIC_ENDPOINTS permitAll; else                  │
│   .anyRequest().authenticated()  ──fail──▶ RestAuthenticationEntryPoint   │
│                        │  (JSON 401 ErrorResponse)                        │
│                        ▼ pass                                             │
│   AuthenticationController  (/api/v1/auth/register|login|refresh|logout|me)│
│                        │                                                   │
│                        ▼                                                   │
│   AuthenticationServiceImpl                                                │
│     register(): existsByEmail → BCrypt.encode → save(User) → issueTokens  │
│     login(): findByEmail → BCrypt.matches → issueTokens                   │
│     refresh(): parse+isRefreshToken → findByTokenId.isValid → ROTATE      │
│                (revoke old, save) → findById(user) → issueTokens (NEW)    │
│     logout(): currentUserId() → revokeAllActiveForUser (bulk UPDATE)       │
│     getCurrentUser(): findById(currentUserId()) → UserMapper              │
│                        │                                                   │
│            issueTokens(): JwtTokenProvider.generateAccessToken            │
│                           JwtTokenProvider.generateRefreshToken(jti)       │
│                           save(new RefreshToken(jti, userId, expiresAt))   │
│                        │                                                   │
│   GlobalExceptionHandler (@RestControllerAdvice) → ErrorResponse JSON      │
│     EmailAlreadyExists→409  InvalidCredentials→401  InvalidRefreshToken→401│
└────────────────────────│───────────────────────────────────────────────────┘
                          │ JDBC
                          ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                              POSTGRESQL                                    │
│  users (V1)                    refresh_tokens (V2)                        │
│   id, email UNIQUE,              id, token_id UUID UNIQUE,                │
│   password_hash, display_name,   user_id FK ON DELETE CASCADE,            │
│   created_at, updated_at         expires_at, revoked, created_at          │
│                                  idx_refresh_tokens_user_id               │
│  flyway_schema_history (Flyway-managed migration ledger)                  │
└───────────────────────────────────────────────────────────────────────────┘
```

**Legend of the two token types flowing through this whole diagram:**

- **ACCESS TOKEN** — short-lived (15m), stateless, never persisted, carries `sub`/`email`/`displayName`/`type=access`, validated purely by signature+exp check in `JwtAuthenticationFilter`.
- **REFRESH TOKEN** — long-lived (7d), carries only `sub`/`jti`/`type=refresh`, mirrored into a `refresh_tokens` row; ROTATED (old revoked, new issued) on every use; revoked in bulk on logout via `revokeAllActiveForUser`.

---

*End of tutorial.*
