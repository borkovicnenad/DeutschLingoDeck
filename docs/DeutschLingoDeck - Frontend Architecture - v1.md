# DeutschLingoDeck – Frontend Architecture

Version: 1.0

---

# 1. Purpose

This document defines the frontend architecture of the DeutschLingoDeck Angular application.

Its purpose is to provide a scalable, maintainable and modular architecture that separates responsibilities, encourages reusable components and aligns the frontend implementation with the backend OpenAPI specification.

This document complements the following project documentation:

- OpenAPI Specification
- Frontend Use Cases
- Screen Specification

The architecture defined here serves as the technical blueprint for the Angular application.

---

# 2. Technology Stack

The frontend application is built using:

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

The frontend follows these principles:

- Feature-based architecture
- Standalone components
- Lazy-loaded routes
- Separation of concerns
- Thin pages
- Reusable UI components
- Business logic remains in backend services
- API communication through dedicated Angular services
- Strong typing throughout the application
- OpenAPI-first development
- Signals for local state
- RxJS for asynchronous workflows

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

HTTP Interceptors

↓

Spring Boot REST API

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
│   ├── config
│   ├── guards
│   ├── interceptors
│   ├── models
│   └── services
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

# 6. Application Bootstrap

When the application starts it should:

1. Load application configuration.
2. Restore authentication session if available.
3. Load the currently authenticated user.
4. Configure global application state.
5. Redirect the user to the appropriate route.

The bootstrap process should be transparent to the user.

---

# 7. Core Layer

The Core layer contains singleton services and application-wide functionality.

Responsibilities include:

- Authentication state
- JWT management
- Current user context
- HTTP configuration
- API configuration
- Route guards
- HTTP interceptors
- Global error handling

The Core layer must never depend on feature modules.

---

# 8. Layout Layer

The Layout layer defines the authenticated application shell.

Responsibilities:

- Header
- Sidebar
- Main content area
- Navigation
- Current user information
- Logout action

Public pages (Login and Registration) must not use the application shell.

---

# 9. Shared Layer

The Shared layer contains reusable UI building blocks.

Examples:

- Loading Spinner
- Confirmation Dialog
- Empty State
- Error Message
- Buttons
- Cards
- Page Header
- Form Controls
- Badges

Shared components must:

- be reusable
- be presentation-focused
- not contain business logic

---

# 10. Feature Layer

Each feature owns all implementation details related to its business domain.

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

Each feature should be as independent as possible.

Communication between features should occur only through shared services.

---

# 11. Routing

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

Authenticated routes must be protected using AuthGuard.

Authenticated users should automatically be redirected away from Login and Registration pages.

---

# 12. State Management

Angular Signals are used for local application state.

Each feature manages its own state.

Typical feature state includes:

- loading
- error
- selected entity
- current filters
- current page
- local UI state

Global application state includes:

- authenticated user
- authentication status
- application configuration

RxJS is used only for:

- HTTP communication
- asynchronous streams
- retry logic
- refresh token flow

NgRx should not be introduced in the MVP.

---

# 13. API Communication

All backend communication must go through dedicated Angular services.

Examples:

- AuthApiService
- ProfileApiService
- DictionaryApiService
- GameApiService
- StatisticsApiService

Components must never communicate directly with HttpClient.

API models must match the backend OpenAPI specification.

---

# 14. Authentication

Authentication uses:

- JWT Access Token
- Refresh Token

Frontend responsibilities:

- Login
- Registration
- Logout
- Automatic token refresh
- Session restoration
- Route protection

The HTTP interceptor automatically attaches the access token to every authenticated request.

---

# 15. Route Guards

The frontend should implement the following guards.

## AuthGuard

Protects authenticated routes.

Redirects unauthenticated users to Login.

---

## GuestGuard

Protects public routes.

Redirects authenticated users to Dashboard.

---

# 16. HTTP Interceptors

The application should contain the following interceptors.

## Authentication Interceptor

Automatically attaches JWT access tokens.

---

## Refresh Token Interceptor

Handles expired access tokens and retries failed requests after obtaining a new token.

---

## Error Interceptor

Converts backend errors into standardized frontend errors.

---

# 17. Forms

Reactive Forms should be used throughout the application.

Examples:

- Login
- Registration
- Profile Update
- Password Change
- Dictionary Import
- Dictionary Metadata
- Game Answer

Template-driven forms should not be used.

---

# 18. Validation

Validation exists on two levels.

## Frontend Validation

Examples:

- required fields
- email format
- password confirmation
- file type
- maximum length

## Backend Validation

Validation errors returned by the backend should be displayed next to the corresponding form fields.

---

# 19. Error Handling

The frontend should gracefully handle:

- Validation Errors
- Business Errors
- Unauthorized Requests
- Forbidden Requests
- Resource Not Found
- Network Errors
- Unexpected Server Errors

Unexpected errors must never crash the application.

---

# 20. Loading Strategy

Every data-driven page must support:

- Loading
- Success
- Empty
- Error

Every form must support:

- Initial
- Validation Error
- Submitting
- Success
- Failure

---

# 21. Responsive Design

The application should support:

- Desktop
- Tablet
- Mobile

The sidebar should automatically collapse into a navigation drawer on smaller screens.

---

# 22. Accessibility

The frontend should follow basic accessibility principles.

Examples:

- Semantic HTML
- Keyboard navigation
- Visible focus indicators
- Proper labels
- ARIA attributes where appropriate
- Sufficient color contrast

---

# 23. Future Enhancements

The following features are intentionally excluded from the MVP:

- Dark Mode
- Push Notifications
- Progressive Web App (PWA)
- Offline Mode
- AI Learning Assistant
- Dictionary Sharing
- Multiplayer Games
- Social Features
- Keyboard Shortcuts

These may be implemented in future versions.

---

# 24. Development Workflow

The recommended implementation order is:

1. Application Shell
2. Authentication
3. Dictionaries
4. Games
5. Statistics
6. Profile
7. UI Polish
8. Performance Optimizations

Each feature should be completed end-to-end before moving to the next feature.

---

# 25. Development Rules for Claude Code

When generating Angular code:

- Follow the OpenAPI specification exactly.
- Follow the Frontend Use Cases document.
- Follow the Screen Specification.
- Use Angular Standalone Components.
- Use feature-based architecture.
- Generate lazy-loaded feature routes.
- Keep pages thin.
- Move reusable UI into the Shared layer.
- Keep business logic inside services.
- Never call HttpClient directly from components.
- Use Angular Signals for local feature state.
- Use Reactive Forms.
- Use Angular Material components where appropriate.
- Use placeholder styling only.
- Do not invent backend endpoints.
- Do not implement backend business logic.
- Keep the code modular, readable and maintainable.
- Ensure the Angular application compiles successfully.