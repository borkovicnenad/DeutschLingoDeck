### Additional change: Card generation based on source and target language

Implement dynamic card generation depending on the selected dictionary language direction.

The import source remains:

`./docs/BusinessDict-dev-with-examples.xlsx`

The Excel structure is:

* Column A → Article
* Column B → Word
* Column C → Grammar Info (plural forms, verb forms, adjective forms, etc.)
* Column D → Example sentence
* Column E → Translation
* Column F → Difficulty
* Column G → Tags

The stored database model should always remain language-independent:

* article
* word
* grammarInfo
* example
* translation
* difficulty
* tags

**Do NOT duplicate data in the database for different language directions.**
Instead, generate the card presentation dynamically depending on the dictionary configuration (`sourceLanguage` and `targetLanguage`).

---

## Case 1

### Source Language = German (`de`)

### Target Language = Croatian (`hr`)

This is the standard German-learning mode.

The card should be rendered as:

**Question side**

Word:

* `Article + Word`
* Example:

  * show the example sentence from Column D underneath
* Grammar:

  * show grammar information from Column C underneath the example

Example:

Word:

```
die Voraussetzung
```

Example:

```
Eine klare Anforderungsanalyse ist die Voraussetzung dafür, dass das Entwicklungsteam später keine unnötigen Rückfragen im Sprint hat.
```

Grammar:

```
Plural: die Voraussetzungen
```

After the answer is revealed:

Translation:

```
preduvjet
```

Continue displaying:

* Example
* Grammar
* Difficulty
* Tags

---

## Case 2

### Source Language = Croatian (`hr`)

### Target Language = German (`de`)

This mode is intended for active German recall.

The **question side** should contain only the Croatian translation.

Question:

```
preduvjet
```

After the answer is revealed:

German:

```
die Voraussetzung
```

Then also display:

Example:

```
Eine klare Anforderungsanalyse ist die Voraussetzung dafür, dass das Entwicklungsteam später keine unnötigen Rückfragen im Sprint hat.
```

Grammar:

```
Plural: die Voraussetzungen
```

Difficulty

Tags

---

## Rendering rules

Never duplicate vocabulary records.

The database should always store:

* article
* word
* translation
* example
* grammarInfo
* difficulty
* tags

Only the UI presentation changes according to the selected language direction.

Implement a small presentation layer (mapper/view model/computed model) that produces the appropriate card representation for each dictionary configuration.

Avoid creating separate entities, DTOs, or duplicated database records for each language pair.

The learning logic, statistics, spaced repetition, and progress tracking should continue to reference the same underlying vocabulary record regardless of language direction.

Update all relevant Angular components, backend DTOs (if necessary), game services, and card rendering logic so that both language directions are fully supported while keeping the domain model normalized.