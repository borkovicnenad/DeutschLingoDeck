# Angular MVP UI Completion Prompt

You are a **Senior Angular Frontend Architect, UI/UX Designer, and Angular Developer**.

Your task is to transform this Angular application into a visually complete MVP.

First, analyze the entire project before making any changes.

Identify every component, page, dialog, widget, or feature that is unfinished, empty, contains placeholder HTML, has missing layouts, or lacks realistic demo content.

Your goal is **not only to complete the layout**, but to make the application look like a polished, production-quality demo suitable for a professional portfolio.

---

# General Requirements

- Analyze the entire project before making any changes.
- Preserve the existing architecture.
- Preserve routing.
- Preserve the component hierarchy.
- Preserve naming conventions.
- Follow Angular 22 best practices.
- Use Signals where appropriate.
- Use modern Angular template syntax (`@if`, `@for`, `@switch`).
- Reuse existing UI patterns.
- Maintain visual consistency across the application.

---

# UI Requirements

Complete every unfinished screen with a realistic enterprise SaaS interface.

Where appropriate, include:

- Page headers
- Descriptions
- Cards
- KPI widgets
- Statistics
- Charts (placeholder/mock if necessary)
- Data tables
- Search
- Sorting
- Filters
- Pagination
- Forms
- Dialogs
- Badges
- Chips
- Avatars
- Breadcrumbs
- Empty states
- Loading states
- Error states
- Confirmation dialogs
- Toolbars
- Contextual action menus

The application should feel like a finished commercial product rather than a prototype.

---

# Demo Data

You are encouraged to create realistic demo data.

You may:

- Create mock datasets
- Hardcode demo entities
- Create fake users
- Create fake job offers
- Create fake applications
- Create fake dashboard statistics
- Create fake notifications
- Create fake activity history
- Create fake AI extraction results

Use realistic values instead of Lorem Ipsum.

Example companies:

- Google
- Microsoft
- Amazon
- Roche
- UBS
- Swisscom
- ELCA
- Adnovum
- Accenture
- SAP

Example technologies:

- Java
- Spring Boot
- Kafka
- Angular
- Kubernetes
- Docker
- PostgreSQL
- Azure
- AWS
- GitLab CI/CD
- RabbitMQ
- Redis
- Elasticsearch
- Terraform

---

# Business Services

You are allowed to modify business services.

If backend functionality is missing:

- Create mock services
- Hardcode realistic sample data
- Simulate API responses
- Simulate loading delays
- Simulate success and error responses where appropriate

The application should behave as if a backend already exists.

Do **not** connect to external APIs.

Keep all mock data localized inside business services so that it can later be replaced with HTTP calls without changing the UI.

---

# Design Guidelines

Follow a modern enterprise dashboard style similar to:

- Jira
- Linear
- GitHub
- Azure Portal
- Notion
- Atlassian
- Material Design 3

Use:

- Clean spacing
- Responsive layouts
- Subtle shadows
- Consistent border radius
- Accessible colors
- Smooth hover animations
- Consistent typography
- Modern icons
- Professional dashboard aesthetics

---

# Code Quality

Prefer reusable components.

Reduce duplicated HTML.

Keep templates clean and readable.

Organize SCSS properly.

Remove obsolete placeholder code whenever possible.

Follow Angular best practices throughout the project.

---

# Final Verification

Before finishing:

- Verify the project builds successfully.
- Fix all TypeScript errors.
- Fix all Angular template errors.
- Remove unused imports.
- Remove dead code introduced by placeholders.
- Ensure every route has a visually complete UI.
- Ensure the overall design is consistent across the application.

---

# Final Report

After completing all work, provide a report containing:

1. Every modified component.
2. Newly created reusable components.
3. Newly created mock services.
4. Newly added demo datasets.
5. Features that still require backend implementation.
6. Suggestions for future improvements.

The final result should resemble a polished enterprise SaaS application suitable for demonstrations, portfolio presentations, and future backend integration.