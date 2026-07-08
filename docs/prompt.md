# Task: Improve Game Card Input UX

Please implement the following frontend improvements in the Angular application.

## Requirements

### 1. Add Skip button next to Submit

On every game card with a text answer input:

- Add a **Skip** button next to the **Submit** button.
- The **Submit** button must remain the primary Enter-key target.
- Pressing **Enter** must still submit the typed answer, not trigger Skip.
- The **Skip** button should only be triggered by an explicit mouse click or keyboard focus + activation.

### 2. Skip counts as an incorrect empty answer

When the user clicks **Skip**:

- Treat the submitted answer as an empty string: `""`.
- Send/evaluate it through the same answer-checking flow as a normal submitted answer.
- The result must be recorded as an incorrect answer.
- The input field should remain required for normal Submit behavior, so users cannot accidentally submit an empty answer.
- Skip is intentional, so it may bypass the required input validation.

### 3. Autofocus input on new card

Whenever a new card is displayed:

- The answer input field must automatically receive focus.
- The user should be able to start typing immediately without clicking into the input.
- This must happen after the card is rendered.
- It should work consistently after:
  - initial game load,
  - Continue to next card,
  - game mode changes if applicable.

## Implementation Notes

- Use Angular best practices for focus handling.
- Avoid arbitrary timeouts unless there is no cleaner alternative.
- Prefer `ViewChild`, lifecycle hooks, signals/effects, or a small reusable focus directive if appropriate.
- Make sure focus does not accidentally move to the Skip button.
- Preserve the existing required validation for normal submissions.
- Avoid duplicate submissions or duplicate backend requests.
- Keep the implementation aligned with the current component structure and styling.

## Acceptance Criteria

- Skip button appears next to Submit.
- Enter submits the written answer.
- Enter does not trigger Skip by default.
- Empty input cannot be submitted normally via Submit.
- Clicking Skip records an incorrect answer with an empty string.
- New card input is focused automatically.
- Existing Continue behavior still works.