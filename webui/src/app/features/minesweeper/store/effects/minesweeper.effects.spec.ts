import { TestBed, inject } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Observable, of } from 'rxjs';

import { MinesweeperEffects } from './minesweeper.effects';

describe('MinesweeperEffects', () => {
  const actions$: Observable<any> = of();
  let effects: MinesweeperEffects;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        MinesweeperEffects,
        provideMockActions(() => actions$)
      ]
    });

    effects = TestBed.get(MinesweeperEffects);
  });

  it('should be created', () => {
    expect(effects).toBeTruthy();
  });
});
