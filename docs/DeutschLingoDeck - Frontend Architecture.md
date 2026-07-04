# DeutschLingoDeck – Frontend Architecture

Version: 1.0

---

# 1. Purpose

This document defines the frontend architecture of the DeutschLingoDeck Angular application.

Its purpose is to provide a scalable, maintainable and modular architecture that separates responsibilities, encourages reusable components and aligns the frontend implementation with the backend OpenAPI specification.

This document complements:

- OpenAPI Specification
- Frontend Use Cases
- Screen Specification

---

# 2. Technology Stack

The frontend is built using the following technologies:

- Angular 22
- TypeScript
- Standalone Components
- Angular Signals
- RxJS
- Angular Router
- Angular HttpClient
- Angular Reactive Forms
- Angular Material

NgModules should not be introduced unless absolutely necessary.

---

# 3. Architectural Principles

The application follows these architectural principles:

- Feature-based architecture
- Standalone components
- Lazy-loaded feature routes
- Thin pages
- Reusable UI components
- Business logic remains in backend services
- API communication through dedicated services
- Local state managed using Angular Signals
- Asynchronous operations handled with RxJS
- Strong typing throughout the application
- OpenAPI-first development
- Separation of concerns

---

# 4. High-Level Architecture

```text
Browser

↓

Angular Application

↓

Feature Pages

↓

Reusable Components

↓

Feature Services

↓

HTTP Interceptor

↓

REST API (Spring Boot)

↓

PostgreSQL
```

---

# 5. Folder Structure

```text
src/app

├── core
│
│   ├── auth
│   ├── guards
│   ├── interceptors
│   ├── services
│   ├── models
│   └── config
│
├── layout
│
│   ├── app-shell
│   ├── header
│   ├── sidebar
│   ├── navigation
│   └── footer
│
├── shared
│
│   ├── components
│   ├── dialogs
│   ├── directives
│   ├── pipes
│   ├── models
│   └── utils
│
├── features
│
│   ├── authentication
│   ├── dashboard
│   ├── dictionaries
│   ├── games
│   ├── statistics
│   └── profile
│
├── assets
│
└── app.routes.ts
```

---

# 6. Core Layer

The Core layer contains singleton services and application-wide functionality.

Responsibilities:

- Authentication state
- JWT handling
- Current user
- HTTP configuration
- API base URL
- Route guards
- HTTP interceptors
- Global error handling

The Core layer must never depend on feature modules.

---

# 7. Layout Layer

The Layout layer defines the authenticated application shell.

It is responsible for:

- Header
- Sidebar
- Main content area
- Navigation
- User information
- Logout action

Public pages such as Login and Registration do not use the application shell.

---

# 8. Shared Layer

The Shared layer contains reusable UI building blocks.

Examples:

- Loading Spinner
- Confirmation Dialog
- Empty State Component
- Error Message Component
- Page Header
- Buttons
- Cards
- Form Controls
- Badges

Shared components must never contain business logic.

---

# 9. Feature Layer

Each feature owns everything related to its business domain.

Example:

```text
features/dictionaries

pages

components

services

models

state

dictionaries.routes.ts
```

Each feature should be independent from other features whenever possible.

---

# 10. Routing

The application uses lazy-loaded feature routes.

Main routes:

```text
/login

/register

/dashboard

/dictionaries

/dictionaries/import

/dictionaries/:dictionaryId

/dictionaries/:dictionaryId/cards/:cardId

/game/start

/game/:gameId

/game/:gameId/summary

/games

/statistics

/profile

/access-denied

/not-found
```

Authenticated routes must be protected by an Auth Guard.

Authenticated users should never access Login or Registration pages.

---

# 11. State Management

Angular Signals are used for local application state.

Examples:

- Current User
- Authentication Status
- Selected Dictionary
- Current Game
- Current Card
- Loading State
- Error State
- Dashboard Statistics

RxJS is used for:

- HTTP requests
- Async streams
- Retry logic
- Token refresh flow

NgRx should not be introduced in the MVP.

---

# 12. API Communication

All backend communication must go through dedicated Angular services.

Examples:

- AuthApiService
- ProfileApiService
- DictionaryApiService
- GameApiService
- StatisticsApiService

Components must never communicate with HttpClient directly.

API models must match the backend OpenAPI specification.

---

# 13. Authentication

Authentication is based on:

- JWT Access Token
- Refresh Token

The frontend is responsible for:

- Login
- Registration
- Logout
- Automatic token refresh
- Session restoration
- Route protection

The HTTP interceptor automatically attaches the access token to every authenticated request.

---

# 14. Guards

The frontend should implement the following route guards:

AuthGuard

Protects authenticated routes.

GuestGuard

Prevents authenticated users from accessing Login and Registration pages.

---

# 15. HTTP Interceptors

The application should contain at least the following interceptors.

Authentication Interceptor

Automatically attaches JWT access tokens.

Refresh Token Interceptor

Automatically refreshes expired access tokens.

Error Interceptor

Transforms backend errors into user-friendly messages.

---

# 16. Forms

Reactive Forms should be used for every form.

Examples:

- Login
- Registration
- Update Profile
- Change Password
- Dictionary Import
- Dictionary Metadata
- Game Answer

Template-driven forms should not be used.

---

# 17. Validation

Validation should exist on two levels.

Frontend Validation

- Required fields
- Email format
- Password confirmation
- File type
- Maximum length

Backend Validation

Validation responses from the backend should be displayed next to the corresponding form fields.

---

# 18. Error Handling

The application should gracefully handle:

- Validation Errors
- Business Errors
- Unauthorized Requests
- Forbidden Requests
- Resource Not Found
- Network Errors
- Unexpected Server Errors

Unexpected errors should never crash the application.

---

# 19. Loading Strategy

Every page that retrieves backend data must support:

- Loading
- Success
- Empty
- Error

Every form should support:

- Initial
- Validation Error
- Submitting
- Success
- Failure

---

# 20. Responsive Design

The application should fully support:

- Desktop
- Tablet
- Mobile

The sidebar should collapse into a navigation drawer on smaller screens.

---

# 21. Accessibility

The frontend should follow basic accessibility guidelines.

Examples:

- Semantic HTML
- Keyboard navigation
- Proper labels
- Focus indicators
- ARIA attributes where appropriate
- Sufficient color contrast

---

# 22. Future Enhancements

The following features are intentionally excluded from the MVP:

- Dark Mode
- Push Notifications
- Progressive Web App
- Offline Mode
- AI Learning Assistant
- Dictionary Sharing
- Multiplayer Games
- Social Features
- Keyboard Shortcuts

These features may be implemented in future versions.

---

# 23. Development Rules for Claude Code

When generating Angular code:

- Follow the OpenAPI specification exactly.
- Follow the Frontend Use Cases document.
- Follow the Screen Specification.
- Use Angular Standalone Components.
- Use feature-based architecture.
- Generate lazy-loaded feature routes.
- Keep pages thin.
- Move reusable UI into Shared components.
- Keep business logic inside services.
- Never call HttpClient directly from components.
- Use Angular Signals for local state.
- Use Reactive Forms.
- Use Angular Material components where appropriate.
- Use placeholder styling only.
- Do not invent backend endpoints.
- Do not implement backend business logic.
- Ensure the application compiles successfully.
- Generate clean, readable and maintainable code suitable for a production-quality portfolio project.