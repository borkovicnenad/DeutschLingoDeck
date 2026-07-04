# DeutschLingoDeck – Screen Specification

Version: 1.0

---

# 1. Purpose

This document defines the user interface screens of the DeutschLingoDeck Angular application.

It serves as the primary frontend functional specification and complements the following documents:

- OpenAPI Specification
- Use Cases
- Frontend Architecture

Each screen describes:

- Purpose
- Route
- Visible UI sections
- User actions
- Backend API calls
- Navigation
- Required UI states

This document is implementation-independent and should guide Angular frontend development.

---

# 2. Public Screens

---

## 2.1 Login

### Route

```text
/login
```

### Purpose

Allows an existing user to authenticate.

### UI Sections

- Application logo
- Welcome message
- Email input
- Password input
- Login button
- Link to registration page
- Validation message area

### User Actions

- Enter email
- Enter password
- Login
- Navigate to Registration

### API Calls

```text
POST /auth/login
```

### Navigation

Success

```text
Dashboard
```

Failure

Remain on Login page.

### States

- Initial
- Loading
- Validation Error
- Authentication Error
- Success

---

## 2.2 Registration

### Route

```text
/register
```

### Purpose

Creates a new user account.

### UI Sections

- Display Name
- Email
- Password
- Confirm Password
- Register button
- Link to Login

### User Actions

- Complete registration form
- Register account

### API Calls

```text
POST /auth/register
```

### Navigation

Success

```text
Dashboard
```

Failure

Remain on Registration page.

### States

- Initial
- Loading
- Validation Error
- Success

---

# 3. Authenticated Layout

---

## 3.1 Application Shell

### Purpose

Provides a common layout for every authenticated screen.

### Sections

Header

Contains:

- Logo
- Current user
- Logout

Sidebar

Contains navigation links:

- Dashboard
- Dictionaries
- Games
- Statistics
- Profile

Main Content

Displays active feature page.

### API Calls

```text
GET /auth/me
POST /auth/logout
```

---

# 4. Dashboard

---

## 4.1 Dashboard

### Route

```text
/dashboard
```

### Purpose

Provides an overview of the user's learning progress.

### UI Sections

- Welcome card
- Overall statistics
- Recent activity
- Quick actions
- Resume active game
- Dictionary overview

### Quick Actions

- Import Dictionary
- Start Game
- View Statistics

### API Calls

```text
GET /statistics
GET /dictionaries
```

### States

- Loading
- Empty
- Success
- Error

---

# 5. Dictionaries

---

## 5.1 Dictionary List

### Route

```text
/dictionaries
```

### Purpose

Displays all dictionaries belonging to the current user.

### UI Sections

Toolbar

- Search
- Import button

Dictionary List

Each row displays:

- Name
- Languages
- Number of cards
- Created date
- Actions

Pagination

### Actions

- Open
- Edit
- Delete
- Start Game

### API Calls

```text
GET /dictionaries
DELETE /dictionaries/{id}
```

### States

- Loading
- Empty
- Success
- Error

---

## 5.2 Dictionary Import

### Route

```text
/dictionaries/import
```

### Purpose

Imports a new dictionary from Excel.

### UI Sections

Import Form

- Excel file
- Dictionary name
- Description
- Source language
- Target language

Import Report

Displays:

- Imported cards
- Skipped rows
- Validation warnings
- Errors

### User Actions

- Select file
- Submit import

### API Calls

```text
POST /dictionaries/import
```

### States

- Initial
- Uploading
- Validation Report
- Success
- Error

---

## 5.3 Dictionary Details

### Route

```text
/dictionaries/:dictionaryId
```

### Purpose

Displays metadata and card preview.

### UI Sections

Header

Dictionary information

Statistics

Card Preview

Action Buttons

- Edit
- Delete
- Start Game

### API Calls

```text
GET /dictionaries/{id}
GET /dictionaries/{id}/cards
```

---

## 5.4 Card Details

### Route

```text
/dictionaries/:dictionaryId/cards/:cardId
```

### Purpose

Displays detailed information about one vocabulary card.

### UI Sections

- Source text
- Translation
- Accepted answers
- Card type
- Article
- Example sentence
- Notes
- Difficulty

### API Calls

```text
GET /dictionaries/{dictionaryId}/cards/{cardId}
```

---

# 6. Games

---

## 6.1 Start Game

### Route

```text
/game/start
```

### Purpose

Allows the user to configure and start a learning session.

### UI Sections

Dictionary selector

Game mode selector

Resume active game banner

Start button

### API Calls

```text
GET /dictionaries
POST /games
```

---

## 6.2 Active Game

### Route

```text
/game/:gameId
```

### Purpose

Displays the active learning session.

### UI Sections

Progress Indicator

Current Card

Answer Input

Validation Feedback

Navigation Buttons

- Submit
- Finish
- Abandon

Timer

### User Actions

Submit answer

Continue

Finish game

Abandon game

### API Calls

```text
GET /games/{gameId}
POST /games/{gameId}/answers
POST /games/{gameId}/finish
POST /games/{gameId}/abandon
```

### States

- Loading
- Playing
- Validating
- Success
- Error

---

## 6.3 Game Summary

### Route

```text
/game/:gameId/summary
```

### Purpose

Displays the final result of a completed game.

### UI Sections

Statistics

Accuracy

Duration

Average response time

Buttons

- Back to Dashboard
- Play Again

### API Calls

```text
GET /games/{gameId}/summary
```

---

## 6.4 Game History

### Route

```text
/games
```

### Purpose

Displays previously completed learning sessions.

### UI Sections

Filters

Table

Pagination

### API Calls

```text
GET /games
```

---

# 7. Statistics

---

## 7.1 Statistics Dashboard

### Route

```text
/statistics
```

### Purpose

Displays detailed learning analytics.

### UI Sections

Overview Cards

Dictionary Statistics

Card Statistics

Learning History

Date Filter

Charts (future enhancement)

### API Calls

```text
GET /statistics
GET /statistics/cards
GET /statistics/dictionaries
GET /statistics/history
```

---

# 8. Profile

---

## 8.1 Profile

### Route

```text
/profile
```

### Purpose

Allows the user to manage account settings.

### UI Sections

Profile Information

Update Profile Form

Change Password Form

### API Calls

```text
GET /profile
PUT /profile
PUT /profile/password
```

---

# 9. Error Screens

---

## 9.1 Access Denied

### Route

```text
/access-denied
```

Displays an HTTP 403 page.

Contains:

- Error message
- Return to Dashboard button

---

## 9.2 Not Found

### Route

```text
/not-found
```

Displays a custom 404 page.

Contains:

- Illustration
- Explanation
- Return to Dashboard button

---

# 10. Common UI States

Every page displaying backend data must support:

- Loading
- Success
- Empty
- Error

Every form must support:

- Initial
- Validation Error
- Submitting
- Success
- Server Error

---

# 11. Responsive Design

The application must support:

Desktop

Tablet

Mobile

The sidebar should collapse into a hamburger menu on smaller devices.

---

# 12. Accessibility

The application should follow basic accessibility guidelines:

- Semantic HTML
- Keyboard navigation
- Visible focus indicators
- Proper form labels
- ARIA attributes where appropriate
- Sufficient color contrast

---

# 13. Future Enhancements

The following features are intentionally excluded from the MVP but should be considered in future versions:

- Dark Mode
- Notifications
- Offline Mode
- Progressive Web App (PWA)
- Keyboard Shortcuts
- Multi-language UI
- Animated Statistics
- AI Learning Assistant
- Dictionary Sharing
- Social Features

---

# 14. Development Rules for Claude Code

When generating the Angular frontend:

- Use Angular 22 standalone components.
- Follow the Frontend Architecture specification.
- Follow the OpenAPI contract exactly.
- Follow the Use Cases document.
- Generate only the defined screens.
- Do not invent additional business functionality.
- Keep components small and reusable.
- Separate pages from reusable components.
- Use lazy-loaded feature routes.
- Use Angular Signals for local state.
- Use Reactive Forms.
- Use Angular Material where appropriate.
- Use placeholder styling only.
- Ensure the application compiles successfully.