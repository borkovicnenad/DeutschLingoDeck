# DeutschLingoDeck – Gameplay, Statistics and Game Engine Improvements

Perform a comprehensive analysis of the entire application (Angular frontend, Spring Boot backend, PostgreSQL database) and implement the following improvements. Before making any changes, identify the root cause of each issue and ensure the final solution is consistent with the existing architecture, OpenAPI specification, and database model.

## 1. Games remain permanently "In Progress"

### Problem
Games that are interrupted (browser refresh, tab close, navigation away, unexpected backend error, etc.) remain forever in the `IN_PROGRESS` state.

### Requirements
- Analyze why games never transition to a final state.
- Implement a robust lifecycle for games.
- Ensure games are completed correctly when:
  - the user finishes normally,
  - the user abandons the game,
  - the browser is refreshed,
  - the browser tab is closed,
  - the user starts another game while one is already running.
- Prevent multiple active games for the same user.
- Consider introducing an `ABANDONED` status if appropriate.
- Statistics must ignore unfinished or abandoned games where necessary.

---

## 2. Every new game always starts with the same first 20 words

### Problem

The game always loads the first 20 dictionary entries instead of selecting cards randomly.

### Requirements

- Investigate the card selection algorithm.
- Randomize card selection.
- Every game should produce a different order.
- Avoid obvious repetition between consecutive games.
- Keep behaviour deterministic only when explicitly needed for testing.

---

## 3. Repeat incorrectly answered cards

Currently every card is asked only once.

Implement a proper spaced repetition mechanism.

Requirements:

- Wrong answers should appear again later during the same game.
- Multiple wrong answers should increase repetition priority.
- Correctly answered cards should gradually disappear from the queue.
- Avoid asking the same card immediately again.
- The algorithm should feel similar to flashcard systems rather than simple looping.

Document the chosen algorithm.

---

## 4. Card statistics do not calculate success rate

Currently card statistics appear incorrect.

Investigate:

- total attempts
- correct answers
- incorrect answers
- success rate
- last played
- average response

Verify that every answer updates statistics correctly.

Success rate should always be recalculated from persisted data rather than cached incorrectly.

---

## 5. Game History duration formatting

Currently durations are displayed as raw seconds.

Convert durations into a human-readable format.

Examples:

- 35 sec
- 2 min 14 sec
- 12 min
- 1 h 8 min

Implement formatting consistently throughout the application.

---

## 6. Statistics filters do not work

The date filters and filtering controls inside the Statistics module currently have no effect.

Investigate the complete flow:

Angular component

↓

HTTP request

↓

Backend controller

↓

Service

↓

Repository

↓

Database query

Fix the entire pipeline.

Filtering should correctly support:

- date from
- date to
- selected dictionary
- selected game mode
- any existing filters in the UI

---

## 7. Mastery Breakdown is unclear

The current "Mastery Breakdown" chart is difficult to understand.

Requirements:

- Analyse how it is currently calculated.
- Verify whether the implementation is mathematically correct.
- If the calculation is poor, redesign it.
- Rename labels if necessary.
- Add meaningful tooltips.
- Clearly explain what every category means.

Example categories could be:

- New
- Learning
- Familiar
- Strong
- Mastered

The calculation should be based on real user performance rather than arbitrary thresholds.

---

## 8. Improve the game engine

Redesign the game engine so that it behaves like a real vocabulary trainer instead of simply iterating through cards.

The engine should consider:

- random card order
- historical success rate
- number of previous mistakes
- cards never answered before
- recently answered cards
- learning priority

Cards with low success rates should appear significantly more often than mastered cards.

The algorithm should balance:

- randomness
- fairness
- repetition
- learning efficiency

Avoid situations where users repeatedly receive the same few cards.

Document the algorithm in comments.

---

# Additional requirements

## Backend

Review:

- entities
- services
- repositories
- statistics calculations
- transactions
- scheduled tasks if needed

Refactor where appropriate.

---

## Frontend

Review:

- statistics pages
- game components
- history page
- charts
- formatting utilities

Improve UX where necessary.

---

## Database

Verify that all statistics are persisted correctly.

If additional columns or tables are needed, create proper Flyway migrations.

Never modify existing migrations.

---

## Code quality

- Remove duplicated logic.
- Improve naming.
- Add comments where algorithms become non-trivial.
- Follow existing project architecture.
- Keep the code production-ready.
- Do not introduce mock data or temporary workarounds.

---

## Deliverables

After implementation provide:

1. Root cause for every issue.
2. Description of every implemented solution.
3. Explanation of the new game engine.
4. Explanation of the mastery calculation.
5. Any database changes.
6. Any API changes.
7. Suggestions for future improvements.