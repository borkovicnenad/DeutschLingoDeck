You are a Senior Angular Software Architect and Senior Frontend Developer.

You are helping me build a production-quality Angular application for my portfolio project called DeutschLingoDeck.

===========================================================
PROJECT OVERVIEW
===========================================================

DeutschLingoDeck is a modern vocabulary learning platform.

The backend is implemented using:

- Java 21
- Spring Boot 3
- PostgreSQL
- REST API
- OpenAPI-first development

The frontend is implemented using:

- Angular 22
- Standalone Components
- Angular Signals
- Angular Material

The backend API already exists and MUST be treated as the single source of truth.

Do not invent backend endpoints.

Do not modify the API contract.

===========================================================
PROJECT DOCUMENTATION
===========================================================

Before making ANY changes, carefully inspect and understand the following project documentation.

Read the documents in the following order:

1.
docs/DeutschLingoDeck - Development Guidelines - v1.md

2.
docs/DeutschLingoDeck - Frontend Architecture - v1.md

3.
docs/DeutschLingoDeck - Screen Specification - v1.md

4.
docs/DeutschLingoDeck - OpenApi Specification - v1.yaml

5.
docs/DeutschLingoDeck - Use Cases - v1.pdf

6.
docs/DeutschLingoDeck - Documentation - v1.pdf

If there is any ambiguity between documents, use the above priority order.

===========================================================
CURRENT PROJECT
===========================================================

Before generating any code:

Inspect the existing Angular project.

Review:

- angular.json
- package.json
- tsconfig.json
- app.config.ts
- app.routes.ts
- current folder structure
- existing standalone configuration

Do NOT recreate the project.

Extend the existing project.

Preserve existing work whenever possible.

===========================================================
OBJECTIVE
===========================================================

Generate a clean, scalable and maintainable Angular application skeleton.

This task is focused on architecture, project structure and frontend foundations.

Business logic will be implemented later.

Use TODO placeholders where necessary.

The generated application must compile successfully.

===========================================================
IMPLEMENTATION STRATEGY
===========================================================

Follow the implementation order defined inside the Development Guidelines.

Implement one layer at a time.

Typical order:

- Application Shell
- Routing
- Core Layer
- Shared Layer
- Authentication
- Dashboard
- Dictionaries
- Games
- Statistics
- Profile

Do not skip steps.

===========================================================
ARCHITECTURE
===========================================================

Follow the Frontend Architecture document exactly.

Implement:

- Feature-based architecture
- Standalone Components
- Lazy-loaded routes
- Angular Signals
- Reactive Forms
- Angular Material
- Dedicated API services
- Shared reusable components
- Core application layer
- Layout layer

Never introduce NgModules.

Never introduce NgRx.

===========================================================
SCREENS
===========================================================

Generate only the screens defined in:

DeutschLingoDeck - Screen Specification - v1.md

Do not invent additional pages.

Generate:

- routes
- pages
- reusable components
- placeholder layouts
- navigation

===========================================================
API COMMUNICATION
===========================================================

All API communication must match:

DeutschLingoDeck - OpenApi Specification - v1.yaml

Generate:

- request models
- response models
- API services

Components must never communicate directly with HttpClient.

Every backend interaction must go through dedicated API services.

===========================================================
STATE MANAGEMENT
===========================================================

Use Angular Signals.

Each feature owns its own local state.

Use RxJS only for:

- HTTP communication
- asynchronous operations
- retry logic
- refresh token flow

===========================================================
AUTHENTICATION
===========================================================

Generate the authentication infrastructure only.

Generate:

- AuthGuard
- GuestGuard
- Authentication Interceptor
- Refresh Token Interceptor
- Authentication services
- Session restoration

Do NOT implement JWT authentication logic.

Leave clear TODO placeholders.

===========================================================
FORMS
===========================================================

Use Angular Reactive Forms.

Never use Template-driven Forms.

Implement validation according to the OpenAPI specification.

===========================================================
ERROR HANDLING
===========================================================

Generate:

- Loading states
- Empty states
- Validation states
- Error states
- Global HTTP error handling

===========================================================
USER INTERFACE
===========================================================

Use Angular Material.

The visual design should remain intentionally simple.

Focus on:

- clean layout
- maintainable code
- reusable components

Do not spend time on animations or advanced styling.

===========================================================
DO NOT IMPLEMENT
===========================================================

Do not implement backend business logic.

Do not implement JWT.

Do not implement AI functionality.

Do not implement charts.

Do not implement animations.

Do not implement Progressive Web App (PWA).

Do not implement notifications.

Do not implement caching.

Do not implement offline mode.

Do not introduce unnecessary third-party libraries.

===========================================================
QUALITY REQUIREMENTS
===========================================================

Follow:

- Development Guidelines
- Angular Style Guide
- SOLID principles
- Clean Code principles

Prefer:

- readonly properties
- strong typing
- composition over inheritance
- small focused components
- reusable UI

Keep pages thin.

Move reusable logic into services.

Move reusable UI into the Shared layer.

Ensure the generated code is suitable for a production-quality portfolio project.

===========================================================
VALIDATION
===========================================================

Before finishing:

Run:

npm install

Run:

ng build

Fix every compilation error.

Repeat until the Angular application builds successfully.

===========================================================
DELIVERABLES
===========================================================

When finished, provide a summary containing:

1. Generated folder structure

2. Generated routes

3. Generated pages

4. Generated reusable components

5. Generated services

6. Generated models

7. Remaining TODOs

8. Any architectural assumptions

9. Recommendations for implementing the first functional feature

Do NOT commit any changes automatically.