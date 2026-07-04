import { CardDetail, CardSummary } from './models/card.model';
import { DictionaryDetail, DictionarySummary } from './models/dictionary.model';

/**
 * Static placeholder content shown immediately on page load and kept on
 * screen if the live request fails, so the Dictionaries feature is always
 * navigable and demonstrates its intended UX. Replaced by real data as soon
 * as the backend responds successfully.
 */
export const SAMPLE_DICTIONARIES: DictionarySummary[] = [
  {
    id: 9001,
    name: 'Everyday Essentials',
    description: 'Core vocabulary for daily conversations.',
    sourceLanguage: 'hr',
    targetLanguage: 'de',
    cardCount: 128,
    createdAt: '2026-04-02T09:15:00Z',
  },
  {
    id: 9002,
    name: 'Business German',
    description: 'Vocabulary for meetings, emails and negotiations.',
    sourceLanguage: 'hr',
    targetLanguage: 'de',
    cardCount: 76,
    createdAt: '2026-05-11T14:30:00Z',
  },
  {
    id: 9003,
    name: 'Travel Phrases',
    description: 'Useful words and phrases for getting around.',
    sourceLanguage: 'hr',
    targetLanguage: 'de',
    cardCount: 54,
    createdAt: '2026-06-01T08:00:00Z',
  },
  {
    id: 9004,
    name: 'Irregular Verbs',
    description: 'The most common irregular German verbs.',
    sourceLanguage: 'hr',
    targetLanguage: 'de',
    cardCount: 40,
    createdAt: '2026-06-20T11:45:00Z',
  },
];

export const SAMPLE_DICTIONARY_DETAIL: DictionaryDetail = {
  ...SAMPLE_DICTIONARIES[0],
  updatedAt: '2026-06-28T17:20:00Z',
};

export const SAMPLE_CARDS: CardSummary[] = [
  { id: 9101, cardType: 'NOUN', article: 'die', sourceText: 'kuća', primaryTranslation: 'das Haus', position: 1 },
  { id: 9102, cardType: 'VERB', sourceText: 'govoriti', primaryTranslation: 'sprechen', position: 2 },
  { id: 9103, cardType: 'NOUN', article: 'der', sourceText: 'grad', primaryTranslation: 'die Stadt', position: 3 },
  { id: 9104, cardType: 'PHRASE', sourceText: 'dobro jutro', primaryTranslation: 'guten Morgen', position: 4 },
  { id: 9105, cardType: 'WORD', sourceText: 'hvala', primaryTranslation: 'danke', position: 5 },
];

export const SAMPLE_CARD_DETAIL: CardDetail = {
  ...SAMPLE_CARDS[0],
  acceptedAnswers: ['das Haus', 'Haus'],
  example: 'Das Haus ist sehr groß.',
  notes: 'Feminine noun in Croatian, neuter in German — a common source of article mistakes.',
  difficultyLevel: 2,
};
