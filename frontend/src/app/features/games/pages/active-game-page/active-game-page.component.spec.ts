import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { ConfirmDialogService } from '../../../../shared/dialogs/confirm-dialog/confirm-dialog.service';
import { AnswerValidationResponse, CurrentCard, Game } from '../../models/game.model';
import { GameApiService } from '../../services/game-api.service';
import { ActiveGamePageComponent } from './active-game-page.component';

describe('ActiveGamePageComponent - Enter key drives Continue', () => {
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

  let gameApi: {
    getById: ReturnType<typeof vi.fn>;
    submitAnswer: ReturnType<typeof vi.fn>;
    finish: ReturnType<typeof vi.fn>;
    abandon: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    gameApi = {
      getById: vi.fn().mockReturnValue(of(initialGame)),
      submitAnswer: vi.fn().mockReturnValue(of(validation)),
      finish: vi.fn().mockReturnValue(of({})),
      abandon: vi.fn().mockReturnValue(of({})),
    };

    await TestBed.configureTestingModule({
      imports: [ActiveGamePageComponent],
      providers: [
        provideRouter([]),
        { provide: GameApiService, useValue: gameApi },
        { provide: ConfirmDialogService, useValue: { confirm: vi.fn().mockReturnValue(of(true)) } },
      ],
    }).compileComponents();
  });

  it('auto-focuses Continue and lets Enter trigger exactly one continue(), matching a click', async () => {
    const fixture = TestBed.createComponent(ActiveGamePageComponent);
    fixture.componentRef.setInput('gameId', '99');
    fixture.detectChanges();
    await fixture.whenStable();

    // Reach the reviewing phase the same way submitAnswer() would.
    const component = fixture.componentInstance as unknown as {
      answerForm: { setValue(v: { answer: string }): void };
      submitAnswer(): void;
    };
    component.answerForm.setValue({ answer: 'Hund' });
    component.submitAnswer();
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const nativeElement = fixture.nativeElement as HTMLElement;
    const continueButton = nativeElement.querySelector(
      '.active-game__feedback button',
    ) as HTMLButtonElement | null;

    expect(continueButton).toBeTruthy();
    expect(document.activeElement).toBe(continueButton);
    expect(gameApi.submitAnswer).toHaveBeenCalledTimes(1);

    continueButton?.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Enter', bubbles: true, cancelable: true }),
    );
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    // Same effect as clicking Continue: feedback gone, next card showing, no duplicate submit.
    expect(nativeElement.querySelector('.active-game__feedback')).toBeNull();
    expect(nativeElement.querySelector('.active-game__prompt')?.textContent).toContain('Katze');
    expect(gameApi.submitAnswer).toHaveBeenCalledTimes(1);
  });
});
