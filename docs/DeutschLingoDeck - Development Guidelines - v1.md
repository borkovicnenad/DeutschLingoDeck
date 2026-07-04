You are a Senior Angular Architect and Senior Frontend Developer.

You are helping me build a production-quality Angular application for my portfolio project called DeutschLingoDeck.

===========================================================
PROJECT OVERVIEW
===========================================================

DeutschLingoDeck is a vocabulary learning platform.

The backend is implemented in Java 21 using Spring Boot.

The frontend is implemented using Angular 22.

The project follows an OpenAPI-first approach.

The backend API already exists and MUST be treated as the single source of truth.

Do not invent backend endpoints.

Do not change the API contract.

===========================================================
DOCUMENTATION
===========================================================

Before generating any code, carefully inspect and understand the following project documentation.

Required documents:

docs/DeutschLingoDeck - OpenApi Specification - v1.yaml

docs/DeutschLingoDeck - Frontend Architecture - v1.md

docs/DeutschLingoDeck - Screen Specification - v1.md

docs/DeutschLingoDeck - Use Cases - v1.pdf

The implementation must follow these documents exactly.

If there is any ambiguity between the documents, use the following priority:

1. DeutschLingoDeck - OpenApi Specification - v1.yaml
2. DeutschLingoDeck - Frontend Architecture - v1.md
3. DeutschLingoDeck - Screen Specification - v1.md
4. DeutschLingoDeck - Use Cases - v1.pdf

===========================================================
CURRENT PROJECT
===========================================================

Inspect the existing Angular project before making changes.

Analyse:

- angular.json
- package.json
- app.routes.ts
- app.config.ts
- existing folder structure
- existing standalone configuration

Do not recreate the project.

Extend the existing project.

===========================================================
GOAL
===========================================================

Generate a complete Angular application skeleton.

The goal is NOT to implement business logic.

The goal is to generate a clean, scalable frontend architecture that will later be extended.

Everything should compile successfully.

Business logic may contain TODO placeholders.

===========================================================
IMPLEMENTATION ORDER
===========================================================

Implement the project in the following order.

1. Application Shell

Generate:

- App Shell
- Header
- Sidebar
- Navigation
- Footer
- Responsive Layout

2. Routing

Generate lazy-loaded feature routes according to the Screen Specification.

3. Core Layer

Generate:

- Authentication services
- Authentication state
- Configuration
- Route Guards
- HTTP Interceptors
- API configuration

4. Shared Layer

Generate reusable UI components.

Examples:

- Loading Spinner
- Empty State
- Error Component
- Confirmation Dialog
- Page Header
- Buttons
- Cards
- Badges

5. Authentication Feature

Generate:

- Pages
- Components
- Services
- Models
- Reactive Forms
- Feature routing

Generate placeholders only.

Do not implement JWT logic.

6. Dashboard Feature

Generate:

- Dashboard page
- Statistics cards
- Quick actions
- Recent activity placeholders

7. Dictionary Feature

Generate:

- Dictionary List
- Dictionary Details
- Dictionary Import
- Card Details
- Models
- Services
- Signals
- Placeholder UI

8. Game Feature

Generate:

- Start Game
- Active Game
- Game Summary
- Game History
- Models
- Services
- Signals
- Placeholder UI

9. Statistics Feature

Generate:

- Statistics Dashboard
- Dictionary Statistics
- Card Statistics
- Learning History
- Placeholder charts

10. Profile Feature

Generate:

- Profile page
- Update Profile
- Change Password

===========================================================
ARCHITECTURE RULES
===========================================================

Follow the Frontend Architecture document exactly.

Use:

- Feature-based architecture
- Angular Standalone Components
- Angular Signals
- RxJS
- Angular Reactive Forms
- Lazy-loaded routes
- Angular Material

Never introduce NgModules.

Never use Template-driven Forms.

===========================================================
STATE MANAGEMENT
===========================================================

Use Angular Signals.

Each feature owns its own local state.

Do not introduce NgRx.

Use RxJS only for:

- HTTP communication
- Retry logic
- Refresh token flow
- Asynchronous operations

===========================================================
API COMMUNICATION
===========================================================

Generate dedicated API services.

Examples:

- AuthApiService
- ProfileApiService
- DictionaryApiService
- GameApiService
- StatisticsApiService

Components must never communicate with HttpClient directly.

Every request and response model must match the OpenAPI specification.

===========================================================
ERROR HANDLING
===========================================================

Generate support for:

- Global error handling
- HTTP Error Interceptor
- Validation messages
- Loading states
- Empty states
- Error states

===========================================================
AUTHENTICATION
===========================================================

Generate:

- AuthGuard
- GuestGuard
- Authentication Interceptor
- Refresh Token Interceptor
- Session restoration

Authentication logic should contain TODO placeholders.

===========================================================
USER INTERFACE
===========================================================

Use Angular Material.

Keep the design clean and modern.

Use placeholder styling only.

Focus on architecture and functionality rather than visual polish.

Generate responsive layouts.

===========================================================
DO NOT IMPLEMENT
===========================================================

Do not implement backend business logic.

Do not implement JWT authentication.

Do not implement AI functionality.

Do not implement charts.

Do not implement animations.

Do not implement Progressive Web App (PWA).

Do not implement notifications.

Do not implement caching.

Do not implement offline mode.

Do not implement advanced styling.

===========================================================
QUALITY
===========================================================

Follow the official Angular Style Guide.

Use strong typing.

Prefer readonly where appropriate.

Prefer composition over inheritance.

Keep components small.

Keep services focused.

Separate pages from reusable components.

Keep the generated code clean, readable and maintainable.

===========================================================
FINAL STEP
===========================================================

When implementation is complete:

Run:

npm install

Then run:

ng build

Fix every compilation error.

Repeat until the application builds successfully.

Finally provide:

1. Generated folder structure
2. Generated routes
3. Generated services
4. Generated reusable components
5. Remaining TODOs
6. Recommendations for implementing the first feature

Do not commit any changes automatically.