export type ValidationResult =
  | 'CORRECT'
  | 'WRONG_ARTICLE'
  | 'WRONG_TRANSLATION'
  | 'TYPO'
  | 'MISSING_WORD'
  | 'EXTRA_WORD'
  | 'PARTIALLY_CORRECT'
  | 'WRONG_SENTENCE';
