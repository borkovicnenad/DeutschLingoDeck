# Frontend Improvements Tutorial

This tutorial walks through three related changes to the DeutschLingoDeck game/study flow, based on three git diffs:

- `git-diff-UI.txt` — Enter-key-driven "Continue" and auto-focus after answering
- `git-diff-Dictionary-change.txt` — direction-aware card rendering (German→Croatian vs. Croatian→German)
- `git-diff-UI-enchancements.txt` — a Skip button, auto-focus on the answer input, and a validation bug fix

All three touch the same feature: the "Active Game" study screen (`frontend/src/app/features/games/pages/active-game-page/`), where a user is shown a vocabulary card, types an answer, and gets feedback before moving to the next card. Read in order — each part assumes the previous one is already in place.

---

## Part 1 — Enter-key drives Continue, without a double-submit (`git-diff-UI.txt`)

### The problem

After submitting an answer, the UI shows a feedback panel with a "Continue" button. Before this change, the only way to move to the next card was to click that button with the mouse — annoying for anyone answering many cards in a row and expecting to just keep typing and pressing Enter.

The naive fix — "listen for Enter anywhere and call `continue()`" — has a trap: a `<button>` that has native browser focus *also* fires a `click` event when the user presses Enter or Space, because that's default HTML button behavior. If you *also* attach your own `(keydown.enter)` handler that calls `continue()`, pressing Enter would fire twice: once from your handler, once from the browser's native "Enter activates the focused button" behavior — a double-advance bug.

### The fix

Two things had to happen together:

**1. Auto-focus the Continue button as soon as it appears**, so pressing Enter naturally lands on it:

```typescript
private readonly continueButton = viewChild<HTMLButtonElement>('continueButton');

constructor() {
  // Auto-focus Continue when it appears so native Enter-on-button already matches a click.
  afterRenderEffect(() => {
    if (this.state.lastValidation()) {
      this.continueButton()?.focus();
    }
  });
}
```

`afterRenderEffect` re-runs after every render where a signal it reads has changed. Here it depends on `state.lastValidation()` — the moment an answer is validated and the feedback panel/Continue button render, this effect fires and focuses the button.

**2. Prevent the native activation from double-firing** by explicitly handling the keydown and calling `preventDefault()`:

```typescript
protected onFeedbackKeydownEnter(event: Event): void {
  // preventDefault stops the focused button's native Enter-triggers-click, avoiding a double continue().
  event.preventDefault();
  this.continue();
}
```

wired up on the feedback container (not the button itself), so it intercepts the Enter keydown *before* the browser's default "activate the focused button" behavior can also fire a click:

```html
<div
  class="active-game__feedback"
  role="status"
  (keydown.enter)="onFeedbackKeydownEnter($event)"
>
  ...
  <button matButton="filled" type="button" (click)="continue()" #continueButton>
    Continue
  </button>
</div>
```

### Why it matters

This is the general pattern for "keyboard shortcut that targets a focusable element": don't rely on the browser's native key-activates-focused-element behavior *and* your own handler at the same time — pick one path and suppress the other with `preventDefault()`. It resurfaces later in Part 3 for the same reason (Skip button vs. Enter-submits-the-form).

### Supporting refactor: explicit "phase" and a staged "next card"

To make the answering → reviewing transition explicit (and to stop the next card from popping in before the user clicks Continue), `active-game-state.service.ts` gained:

```typescript
export type GamePhase = 'answering' | 'reviewing';
```

```typescript
readonly phase = computed<GamePhase>(() => (this.lastValidationSignal() ? 'reviewing' : 'answering'));
```

and a staging signal so the next card's data is fetched with the validation response but not displayed until `continueToNextCard()` actually runs:

```typescript
// Holds the next card once it's known, without displaying it — it is only
// merged into `game.currentCard` when continueToNextCard() runs.
private readonly pendingNextCardSignal = signal<CurrentCard | undefined>(undefined);
```

```typescript
continueToNextCard(): void {
  const nextCard = this.pendingNextCardSignal();
  this.gameSignal.update((game) => (game ? { ...game, currentCard: nextCard } : game));
  this.pendingNextCardSignal.set(undefined);
  this.lastValidationSignal.set(null);
  this.questionStartedAt = Date.now();
}
```

Previously, `currentCard` was overwritten immediately inside the `submitAnswer` response handler — meaning the next card's text was already sitting in the DOM (just hidden behind the feedback panel) the instant the answer was submitted. Staging it separately keeps the "reviewing" phase strictly about the *answered* card until the user is ready to move on.

---

## Part 2 — Direction-aware card generation (`git-diff-Dictionary-change.txt`)

This is the largest of the three changes. It touches the backend Spring Boot service, the database-facing DTOs, and the Angular rendering layer.

### The problem

DeutschLingoDeck stores vocabulary as a single, language-neutral `Card` (article, word, grammar info, example, translation, difficulty, tags), owned by a `Dictionary` that has a `sourceLanguage` and `targetLanguage` (e.g. `de`→`hr`, or `hr`→`de`). The requirement: the **same** underlying `Card` row must be presented two different ways depending on which direction its dictionary is configured for — **without duplicating any data**:

- **German → Croatian** ("Case 1", produce a translation): show the German word (article + word) plus its example and grammar info up front; reveal the Croatian translation (plus difficulty/tags) after the answer.
- **Croatian → German** ("Case 2", recall German): show *only* the Croatian translation up front; reveal the German word, example, grammar info, difficulty, and tags after the answer.

Three sub-problems fell out of implementing this:

1. The entity used a generic `notes` field where the domain concept is really "grammar info" (plural/verb/adjective forms) — worth renaming for clarity before building direction logic on top of it.
2. The real import file (`docs/BusinessDict-dev-with-examples.xlsx`) has **no header row** — it's fixed columns A–G (article, word, grammar, example, translation, difficulty, tags) starting at row 1. The existing importer only understood header-row-plus-synonym-matching files, so this file would silently import zero cards.
3. Answer grading was direction-blind: it always checked the typed answer against the translation. For "recall German" dictionaries, the correct answer becomes the German word instead.

### Step 1: rename `notes` → `grammarInfo`

Mechanical, but touches every layer — entity, request/response DTOs, the import service's header-synonym table, and the Angular models/forms:

```java
// Card.java
private String grammarInfo;   // was: private String notes;

public String getGrammarInfo() { return grammarInfo; }
public void setGrammarInfo(String grammarInfo) { this.grammarInfo = grammarInfo; }
```

A Flyway migration renames the column instead of dropping/re-adding it, preserving existing data:

```sql
ALTER TABLE cards RENAME COLUMN notes TO grammar_info;
```

### Step 2: teach the importer to recognize a headerless file

The importer's header-matching logic normalizes column headers and looks them up in a synonym table:

```java
private static final Map<String, String> HEADER_SYNONYMS = Map.ofEntries(
    Map.entry("word", "sourceText"),
    Map.entry("translation", "primaryTranslation"),
    Map.entry("grammarinfo", "grammarInfo"),
    Map.entry("grammar", "grammarInfo"),
    Map.entry("notes", "grammarInfo"),   // old synonym still accepted, now maps to the new field
    // ...
);
```

The fix adds a second layout, selected by whether the *first row* looks like a header at all:

```java
private static final Set<String> CANONICAL_FIELDS = Set.copyOf(HEADER_SYNONYMS.values());

/** Column order (A-G) for a headerless business-dictionary-style import file. */
private static final List<String> POSITIONAL_FIELDS = List.of(
    "article", "sourceText", "grammarInfo", "example", "primaryTranslation", "difficultyLevel", "tags");

/** A file is headerless when its first row matches none of the recognized canonical field names. */
private boolean looksLikeHeaderRow(List<String> rawFirstRow) {
    return rawFirstRow.stream().map(this::normalizeHeader).anyMatch(CANONICAL_FIELDS::contains);
}
```

`parseWorkbook` (and `parseCsv`, for consistency) now branch on this check:

```java
boolean looksLikeHeader = looksLikeHeaderRow(rawFirstRow);
List<String> headers = looksLikeHeader
        ? rawFirstRow.stream().map(this::normalizeHeader).toList()
        : POSITIONAL_FIELDS;
int firstDataRowNum = looksLikeHeader ? sheet.getFirstRowNum() + 1 : sheet.getFirstRowNum();
```

If it looks like a header, behavior is unchanged (row 0 is consumed as the header, data starts at row 1). If it doesn't, the fixed `POSITIONAL_FIELDS` list is used as the "header," and — critically — **row 0 is included as data**, since it wasn't actually a header row. This kept every existing header-based test (and the existing `sample_vocabulary.xlsx`) working unchanged, while making the real business-dictionary file importable for the first time. A new test (`importDictionary_parsesHeaderlessPositionalRows`) locks in the positional behavior with a two-row CSV shaped exactly like the real file.

### Step 3: redact the "answer" field on the question side

`CurrentCardResponse` — what the frontend receives before the user answers — used to always include `sourceText`/`article`/`example`, direction-agnostic:

```java
public record CurrentCardResponse(
    Long id,
    String sourceText,
    CardType cardType,
    String article,
    String example
) {}
```

It's now built by hand (not a straight MapStruct passthrough) in `GameServiceImpl`, and whichever field represents "the answer" for this dictionary's direction is `null`:

```java
/** The language a card's article/sourceText are always recorded in. */
private static final String GERMAN_LANGUAGE = "de";

/** true when this dictionary's direction is "recall German" (translation shown first). */
private boolean isGermanRecall(Dictionary dictionary) {
    return !GERMAN_LANGUAGE.equalsIgnoreCase(dictionary.getSourceLanguage());
}

private CurrentCardResponse resolveCurrentCard(Game game) {
    // ... load card ...
    if (isGermanRecall(card.getDictionary())) {
        // Case 2: only the translation is shown; German word/example/grammar stay hidden.
        return new CurrentCardResponse(
                card.getId(), card.getCardType(), null, null, card.getPrimaryTranslation(), null, null);
    }
    // Case 1: German word/example/grammar are shown; translation stays hidden.
    return new CurrentCardResponse(
            card.getId(), card.getCardType(), card.getArticle(), card.getSourceText(), null,
            card.getExample(), card.getGrammarInfo());
}
```

This matters for more than just UI tidiness: before this change, a curious user could open devtools and read the answer straight out of the network response before submitting. Redacting server-side, rather than just hiding fields in the template, actually protects the answer.

Direction itself is derived from a single, simple rule — `dictionary.sourceLanguage == "de"` → Case 1, anything else → Case 2 — rather than hardcoding `"hr"` anywhere. The underlying `Card` never changes; only which of its fields get sent (and when) depends on the owning `Dictionary`'s configured direction.

### Step 4: send back the full card once the user has answered

Once an answer is submitted, hiding data no longer serves any purpose — the user has already committed to an answer, so the full card can be revealed. A new DTO carries it:

```java
/** The full, unredacted card - safe to send once the user has already submitted an answer. */
public record CardRevealResponse(
    String article,
    String sourceText,
    String primaryTranslation,
    String example,
    String grammarInfo,
    Integer difficultyLevel,
    List<String> tags
) {}
```

added as a new field on the existing response, rather than a new endpoint:

```java
public record AnswerValidationResponse(
    ValidationResult result,
    Boolean correct,
    String givenAnswer,
    String expectedAnswer,
    String message,
    CardRevealResponse revealedCard,   // new
    CurrentCardResponse nextCard
) {}
```

### Step 5: make grading direction-aware

Before this change, `AnswerValidator` was tightly coupled to `Card` and always graded against the translation:

```java
public ValidationResult validate(Card card, String givenAnswer) {
    List<String> accepted = card.getAcceptedAnswers();
    // ...falls back to card.getPrimaryTranslation()...
}
```

Interesting detail found while reading the existing code: the validator already had a "wrong article" heuristic (comparing `der`/`die`/`das` prefixes) — logic that only makes sense if you're grading a *German* answer. Since accepted answers were always populated with the *translation*, that branch was effectively dead code. Making direction explicit both implements Case 2 and gives that heuristic a real purpose.

The fix decouples the validator from `Card` entirely — it becomes pure string comparison:

```java
public ValidationResult validate(List<String> accepted, CardType cardType, String givenAnswer) {
    // same comparison heuristics as before, just parameterized instead of reading from `card`
}
```

and `GameServiceImpl` now decides *what counts as correct* based on direction, before calling it:

```java
/** The answer(s) that count as correct for this card, given its dictionary's learning direction. */
private List<String> expectedAnswers(Card card) {
    if (isGermanRecall(card.getDictionary())) {
        String germanForm = card.getArticle() != null
                ? card.getArticle() + " " + card.getSourceText()
                : card.getSourceText();
        return List.of(germanForm);   // Case 2: grade against "die Voraussetzung"
    }
    if (card.getAcceptedAnswers() != null && !card.getAcceptedAnswers().isEmpty()) {
        return card.getAcceptedAnswers();
    }
    return card.getPrimaryTranslation() != null ? List.of(card.getPrimaryTranslation()) : List.of();
}
```

```java
List<String> expected = expectedAnswers(card);
ValidationResult result = answerValidator.validate(expected, card.getCardType(), request.answer());
```

### Step 6: the frontend presentation layer

The Angular side deliberately does **not** receive an explicit "direction" flag. Instead, direction is inferred purely from *which fields the backend already redacted* — a small, pure-function presentation layer reads that shape:

```typescript
// card-presentation.util.ts
export function questionView(card: CurrentCard): QuestionView {
  if (card.sourceText) {
    // Case 1: German word is present, so it's the question; example/grammar accompany it.
    return {
      primary: [card.article, card.sourceText].filter(Boolean).join(' '),
      example: card.example,
      grammarInfo: card.grammarInfo,
    };
  }
  // Case 2: sourceText was redacted, so the translation is the question.
  return { primary: card.primaryTranslation ?? '' };
}

export function revealView(question: CurrentCard, reveal: CardReveal): RevealView {
  const primary = question.sourceText
    ? (reveal.primaryTranslation ?? '')                          // Case 1: reveal the translation
    : [reveal.article, reveal.sourceText].filter(Boolean).join(' '); // Case 2: reveal the German word

  return { primary, example: reveal.example, grammarInfo: reveal.grammarInfo,
           difficultyLevel: reveal.difficultyLevel, tags: reveal.tags };
}
```

The component just wires these into computed signals and renders whatever comes back — the template has no `if (direction === ...)` branching at all:

```typescript
protected readonly question = computed(() => {
  const card = this.state.game()?.currentCard;
  return card ? buildQuestionView(card) : null;
});

protected readonly reveal = computed(() => {
  const card = this.state.game()?.currentCard;
  const revealedCard = this.state.lastValidation()?.revealedCard;
  return card && revealedCard ? buildRevealView(card, revealedCard) : null;
});
```

```html
@if (question(); as q) {
  <p class="active-game__prompt">{{ q.primary }}</p>
  @if (q.example) { <p class="active-game__example">{{ q.example }}</p> }
  @if (q.grammarInfo) { <p class="active-game__grammar">Grammar: {{ q.grammarInfo }}</p> }
}
```

and the feedback panel's old, minimal "Expected: ..." line is replaced with the full reveal — headline term, example, grammar, difficulty, and tags as chips:

```html
@if (reveal(); as r) {
  <p class="active-game__reveal-primary">{{ r.primary }}</p>
  @if (r.example) { <p class="active-game__example">{{ r.example }}</p> }
  @if (r.grammarInfo) { <p class="active-game__grammar">Grammar: {{ r.grammarInfo }}</p> }
  @if (r.difficultyLevel !== null && r.difficultyLevel !== undefined) {
    <p>Difficulty: {{ r.difficultyLevel }} / 5</p>
  }
  @if (r.tags?.length) {
    <mat-chip-set>
      @for (tag of r.tags; track tag) { <mat-chip>{{ tag }}</mat-chip> }
    </mat-chip-set>
  }
}
```

### Why this design

- **No duplicated data**: one `Card` row serves both directions; only the *presentation* (and, server-side, which fields are redacted pre-answer) changes.
- **Direction lives in exactly one place per tier**: `isGermanRecall()` on the backend, "which field is populated" inference on the frontend. Neither has to be told the direction twice.
- **Learning/progress logic is untouched**: `CardProgress`, `SpacedRepetitionScheduler`, and statistics all still key off the same `Card`/`CardProgress` rows regardless of direction — only the question/answer/grading *view* of a card changed.

---

## Part 3 — Skip button, autofocus, and a validation bug fix (`git-diff-UI-enchancements.txt`)

### The requirements

Three UX asks for the answer form:

1. A **Skip** button next to **Submit** — Enter must still submit the typed answer, never trigger Skip.
2. Skip submits an empty string through the *same* grading flow, always recorded as incorrect, deliberately bypassing the "required" validation that Submit still enforces.
3. The answer `<input>` should auto-focus on every new card (initial load and after Continue).

### Skip button: type="button" solves the Enter-conflict for free

Unlike the Continue-button/Enter conflict in Part 1, this one didn't need a `preventDefault()` dance. As long as Skip is `type="button"` (not `type="submit"`) and isn't the focused element while the user is typing, pressing Enter inside the input naturally triggers the form's `(ngSubmit)` — which calls `submitAnswer()`, not Skip:

```html
<div class="active-game__form-actions">
  <button matButton="filled" type="submit" [disabled]="state.submitting()">
    @if (state.submitting()) { Validating... } @else { Submit }
  </button>
  <button matButton="outlined" type="button" [disabled]="state.submitting()" (click)="skipAnswer()">
    Skip
  </button>
</div>
```

### Skip bypasses the form's validation, on purpose

`submitAnswer()` refuses to proceed if the reactive form is invalid (empty input, required validator). `skipAnswer()` deliberately doesn't check the form at all — it calls the same state method directly with `''`:

```typescript
protected skipAnswer(): void {
  const currentCard = this.state.game()?.currentCard;
  if (!currentCard || this.state.submitting()) {
    return;
  }

  // Skip intentionally bypasses the Submit button's required validation - it always
  // records an empty answer, regardless of whatever partial text is in the field.
  this.state.submitAnswer(this.gameIdAsNumber(), currentCard.id, '');
}
```

Guarding on `this.state.submitting()` (mirrored by `[disabled]="state.submitting()"` on the button) prevents a double-request if the user clicks Skip while a previous submission is still in flight.

### The bug this surfaced: the backend rejected the empty answer

Manual testing after wiring up the frontend produced a generic **"Couldn't load this game"** error the moment Skip was clicked — misleading, since the failure had nothing to do with loading. The actual cause: `SubmitAnswerRequest.answer` was annotated `@NotBlank @Size(min = 1, max = 500)`, so Spring's validation rejected the empty string with an HTTP 400 before it ever reached the game logic — and the frontend happened to reuse the same error banner/message for *any* request failure on this page, load or submit.

The fix: relax the constraint to allow (but not require null on) an empty string:

```java
/** {@code answer} may be an empty string - the frontend's Skip action submits "" as an intentional, incorrect answer. */
public record SubmitAnswerRequest(
    @NotNull Long cardId,
    @NotNull @Size(max = 500) String answer,   // was: @NotBlank @Size(min = 1, max = 500)
    @NotNull @PositiveOrZero Long responseTimeMs
) {}
```

Nothing downstream needed to change: `AnswerValidator` already treats an unmatched empty string as an incorrect answer, and the `given_answer` database column allows empty strings (only `NULL` is disallowed). A regression test (`GameServiceSubmitAnswerTest`) locks in that an empty answer is accepted end-to-end and correctly increments `incorrectAnswers`.

**Lesson**: when a UI feature deliberately needs to send input that would normally be considered "invalid" (like an intentional empty answer), check the *backend* validation, not just the frontend form — bypassing client-side validation is meaningless if the server rejects the request anyway. And a generic, reused error banner can easily point you at the wrong subsystem when debugging — it's worth checking the actual network response/status code rather than trusting the displayed message.

### Autofocus: the `viewChild` + `matInput` gotcha

Extending the auto-focus pattern from Part 1 to the answer input looked like a one-line addition:

```typescript
private readonly answerInput = viewChild<HTMLInputElement>('answerInput');
// ...
this.answerInput()?.focus();
```

This compiled fine but failed at runtime with `this.answerInput(...)?.focus is not a function`. The reason: `viewChild('templateRef')` resolves to the plain native element only when the tagged element has no directive that requests injecting `ElementRef` for itself — Angular Material's `MatInput` directive (applied via the `matInput` attribute) does exactly that, so the query resolves to an `ElementRef<HTMLInputElement>` wrapper instead of the raw `HTMLInputElement`. The earlier `#continueButton` case worked fine only because a plain `matButton`-directive button doesn't trigger this behavior.

The fix: type the query as `ElementRef<HTMLInputElement>` and call `.nativeElement.focus()`:

```typescript
private readonly answerInput = viewChild<ElementRef<HTMLInputElement>>('answerInput');

constructor() {
  afterRenderEffect(() => {
    if (this.state.lastValidation()) {
      this.continueButton()?.focus();
    } else if (this.state.game()?.currentCard) {
      // Auto-focus the answer input for every new card (initial load and Continue alike).
      this.answerInput()?.nativeElement.focus();
    }
  });
}
```

Reusing the same `afterRenderEffect` from Part 1 (rather than adding a second one) also naturally satisfies "auto-focus on initial load and after Continue": the effect re-runs whenever `state.lastValidation()` or `state.game()` change, which covers both cases without extra wiring — it focuses Continue while there's a validation result to review, and the answer input the rest of the time a card is showing.

**Lesson**: when a `viewChild`/`ViewChild` query on a native element unexpectedly returns something without the DOM methods you expect, check whether any directive on that element (particularly Angular Material directives) requests `ElementRef` injection — it changes what the query resolves to.

---

## Testing notes

Each part came with test coverage aimed at the specific behavior it changed, not just "does it compile":

- **Backend** (`DictionaryImportServiceTest`): a new test feeds the importer a headerless, positionally-laid-out CSV shaped like the real business dictionary file, asserting cards are still parsed correctly — locking in the header/headerless detection logic.
- **Backend** (`GameServiceSubmitAnswerTest`): asserts that submitting an empty answer is accepted, graded incorrect, and increments the game's `incorrectAnswers` counter — the regression test for the Skip validation bug.
- **Frontend** (`active-game-page.component.spec.ts`): covers the Enter-key/Continue double-fire guard, auto-focus on initial load, Skip's empty-answer submission, and re-focus after Continue — each as its own targeted spec rather than one broad end-to-end test.

In all three parts, the tests were added at the exact seam where a regression would otherwise be invisible until manual testing (or a user) hit it — the headerless-import test would have caught the "file silently imports nothing" gap immediately; the empty-answer test would have caught the 400 error before it ever reached a browser.
