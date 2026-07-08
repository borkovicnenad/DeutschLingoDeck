import { TestBed } from '@angular/core/testing';
import { Subject, of } from 'rxjs';

import { AnswerValidationResponse, CurrentCard, Game } from '../models/game.model';
import { GameApiService } from '../services/game-api.service';
import { ActiveGameStateService } from './active-game-state.service';

describe('ActiveGameStateService', () => {
  let service: ActiveGameStateService;
  let gameApi: { getById: ReturnType<typeof vi.fn>; submitAnswer: ReturnType<typeof vi.fn> };

  const firstCard: CurrentCard = { id: 1, sourceText: 'Hund', cardType: 'WORD' };
  const secondCard: CurrentCard = { id: 2, sourceText: 'Katze', cardType: 'WORD' };

  const initialGame: Game = {
    id: 99,
    dictionaryId: 1,
    status: 'IN_PROGRESS',
    totalCards: 2,
    answeredCards: 0,
    currentCard: firstCard,
  };

  const validation: AnswerValidationResponse = {
    result: 'CORRECT',
    correct: true,
    givenAnswer: 'Hund',
    expectedAnswer: 'Hund',
    nextCard: secondCard,
  };

  beforeEach(() => {
    gameApi = {
      getById: vi.fn().mockReturnValue(of(initialGame)),
      submitAnswer: vi.fn().mockReturnValue(of(validation)),
    };

    TestBed.configureTestingModule({
      providers: [ActiveGameStateService, { provide: GameApiService, useValue: gameApi }],
    });
    service = TestBed.inject(ActiveGameStateService);
    service.load(99);
  });

  it('keeps the answered card visible and does not reveal the next card until continue is called', () => {
    service.submitAnswer(99, firstCard.id, 'Hund');

    expect(service.lastValidation()).toEqual(validation);
    expect(service.phase()).toBe('reviewing');
    expect(service.game()?.currentCard).toEqual(firstCard);
    expect(service.game()?.answeredCards).toBe(1);
  });

  it('only swaps in the next card once continueToNextCard runs', () => {
    service.submitAnswer(99, firstCard.id, 'Hund');

    service.continueToNextCard();

    expect(service.lastValidation()).toBeNull();
    expect(service.phase()).toBe('answering');
    expect(service.game()?.currentCard).toEqual(secondCard);
  });

  it('ignores a second submitAnswer call while one is already in flight', () => {
    const pending = new Subject<AnswerValidationResponse>();
    gameApi.submitAnswer.mockReturnValue(pending);

    service.submitAnswer(99, firstCard.id, 'Hund');
    service.submitAnswer(99, firstCard.id, 'Hund');

    expect(gameApi.submitAnswer).toHaveBeenCalledTimes(1);

    pending.next(validation);

    expect(service.lastValidation()).toEqual(validation);
  });
});
