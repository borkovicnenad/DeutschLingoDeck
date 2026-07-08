import { CardReveal, CurrentCard } from './game.model';

/** What to render on the question side, before an answer is submitted. */
export interface QuestionView {
  primary: string;
  example?: string | null;
  grammarInfo?: string | null;
}

/** What to render once the answer has been revealed. */
export interface RevealView {
  primary: string;
  example?: string | null;
  grammarInfo?: string | null;
  difficultyLevel?: number | null;
  tags?: string[];
}

/**
 * The backend already redacts whichever field is the "answer" for this dictionary's learning
 * direction, so the question view is inferred from which fields are present - no separate
 * direction flag needs to travel over the wire.
 */
export function questionView(card: CurrentCard): QuestionView {
  if (card.sourceText) {
    return {
      primary: [card.article, card.sourceText].filter(Boolean).join(' '),
      example: card.example,
      grammarInfo: card.grammarInfo,
    };
  }
  return { primary: card.primaryTranslation ?? '' };
}

export function revealView(question: CurrentCard, reveal: CardReveal): RevealView {
  const primary = question.sourceText
    ? (reveal.primaryTranslation ?? '')
    : [reveal.article, reveal.sourceText].filter(Boolean).join(' ');

  return {
    primary,
    example: reveal.example,
    grammarInfo: reveal.grammarInfo,
    difficultyLevel: reveal.difficultyLevel,
    tags: reveal.tags,
  };
}
