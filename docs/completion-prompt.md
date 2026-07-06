# DeutschLingoDeck – Full-Stack Completion Prompt

You are a **Senior Full-Stack Software Architect and Developer**.

Analyze the entire **DeutschLingoDeck** project, including:

- Angular frontend
- Spring Boot backend
- Database model
- API contracts
- Authentication/authorization
- Business services
- Existing architecture
- Existing TODOs, placeholders, incomplete components, and missing logic

Your task is to make the application functionally complete as a working full-stack MVP.

---

# Main Goal

Complete the application end-to-end so that every major feature works from:

**Angular UI → Angular service → REST API → Spring Boot service → database → response back to UI**

The application should no longer be only a visual prototype. It should behave like a real learning application.

---

# Project Context

DeutschLingoDeck is a German vocabulary learning application with:

- user accounts
- dictionaries
- vocabulary cards
- learning sessions
- spaced repetition
- statistics
- dashboard
- gamification
- AI-assisted features
- custom dictionary import, preferably from Excel/CSV
- user-specific data

---

# General Rules

- Analyze the whole project before making changes.
- Preserve the existing architecture where reasonable.
- Do not rewrite the project from scratch.
- Keep package/component naming consistent.
- Follow Angular 22 best practices.
- Follow Spring Boot best practices.
- Keep code clean, readable, and maintainable.
- Prefer small focused services and components.
- Remove obsolete mock-only logic once real backend integration exists.
- Keep realistic fallback demo data only where backend implementation is not yet possible.
- Do not connect to external paid APIs unless explicitly configured.
- Do not break existing working features.

---

# Backend Requirements

Complete the Spring Boot backend.

Implement or finish:

- entities
- DTOs
- repositories
- services
- service implementations
- controllers
- mappers
- validation
- exception handling
- database relationships
- migrations if the project uses Flyway/Liquibase
- authentication and user-specific data ownership
- REST endpoints needed by the frontend

All important data must be persisted in the database.

---

# Database Requirements

Review the current database schema and complete it if necessary.

The database should support:

- users
- dictionaries
- vocabulary cards
- card examples
- learning progress
- review history
- spaced repetition scheduling
- statistics
- achievements
- imported dictionaries
- AI extraction results if already planned

Ensure correct relationships, for example:

- one user can have many dictionaries
- one dictionary can have many cards
- one user can have progress for many cards
- one card can have many review records

Use proper constraints, indexes, and timestamps where appropriate.

---

# Frontend Requirements

Connect the Angular frontend to the real backend.

Implement or complete:

- API services
- models/interfaces
- state handling
- forms
- validation
- loading states
- error states
- success states
- routing integration
- dashboard data loading
- dictionary management
- card management
- learning session flow
- statistics views
- import flow
- user-specific views

Replace hardcoded UI data with backend calls where possible.

---

# Core Business Logic

Implement the main business logic of the application.

At minimum, support:

## Dictionaries

- list dictionaries of the logged-in user
- create dictionary
- edit dictionary
- delete dictionary
- view dictionary details
- count cards per dictionary
- mark dictionary as active or selectable for learning sessions

## Vocabulary Cards

- create card
- edit card
- delete card
- list cards by dictionary
- search cards
- filter cards by difficulty, tag, learning status, or due date
- store German word
- store translation
- store example sentence
- store notes
- store tags
- store difficulty level

## Learning Sessions

Implement a functional flashcard learning flow:

- user selects dictionary
- backend returns due cards
- user reviews cards one by one
- user submits result, for example `again`, `hard`, `good`, `easy`
- backend updates learning progress
- backend stores review history
- frontend displays progress during the session
- frontend shows session summary at the end

## Spaced Repetition

Implement a simple but real spaced repetition algorithm.

Use a maintainable MVP approach, for example:

- `again` → review soon
- `hard` → short interval
- `good` → normal interval
- `easy` → longer interval

Track:

- ease factor
- interval
- repetitions
- due date
- last reviewed date
- review count
- correct/incorrect count

The algorithm does not need to be perfect, but it must be consistent and testable.

## Statistics

Calculate real statistics from persisted data:

- total dictionaries
- total cards
- cards learned
- cards due today
- review accuracy
- learning streak
- daily review count
- weekly activity
- mastered cards
- difficult cards
- progress per dictionary

## Gamification

Implement basic gamification:

- XP calculation
- level calculation
- achievements
- daily goal progress
- learning streak
- badges where reasonable

Persist user progress where needed.

## Import

If the project already contains or plans custom dictionary import, implement it.

Support at least one practical import format:

- CSV, or
- Excel if existing dependencies allow it

The import should allow a user to upload a dictionary file containing vocabulary data.

Expected columns may include:

- German word
- Translation
- Example sentence
- Difficulty
- Tags
- Notes

Validate imported rows and return useful error messages.

---

# Authentication and Authorization

Ensure user-specific data isolation.

A user must only access their own:

- dictionaries
- cards
- learning progress
- statistics
- imports
- achievements

If authentication already exists, integrate all endpoints with it.

If authentication is incomplete, implement a simple secure MVP approach consistent with the current project architecture.

Do not expose global data accidentally.

---

# API Design

Create clean REST APIs.

Use consistent endpoint naming, for example:

```text
/api/dictionaries
/api/dictionaries/{id}
/api/dictionaries/{id}/cards
/api/cards/{id}
/api/learning/sessions
/api/learning/review
/api/statistics/dashboard
/api/import/dictionaries
```

Use proper HTTP methods:

```text
GET     read data
POST    create data or perform actions
PUT     replace/update full resource
PATCH   partial update
DELETE  delete resource
```

Return meaningful status codes and error messages.

---

# Error Handling

Implement consistent error handling across backend and frontend.

Backend should return structured error responses.

Frontend should display:

- validation errors
- loading indicators
- empty states
- backend errors
- success notifications

Do not silently fail.

---

# Testing and Verification

Add or update tests where reasonable.

At minimum:

- backend service tests for spaced repetition logic
- backend tests for dictionary/card ownership rules
- frontend should compile without template/type errors
- backend should start successfully
- database migrations should run successfully

Before finishing, run:

```bash
npm install
npm run build
```

or the project-specific frontend build command.

Also run:

```bash
mvn test
mvn spring-boot:run
```

or the project-specific backend commands.

Fix all errors introduced by your changes.

---

# Final Report

After completing the implementation, provide a clear report with:

1. What was analyzed.
2. What frontend files were changed.
3. What backend files were changed.
4. What database changes were added.
5. What business logic was implemented.
6. What API endpoints are now available.
7. What features are fully functional.
8. What features are still mocked or incomplete.
9. How to start the application.
10. How to test the main user flows.

The final result should be a functional full-stack MVP where the user can create dictionaries, add cards, run learning sessions, persist progress, view statistics, and use the application end-to-end.