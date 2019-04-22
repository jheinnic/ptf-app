import { TestBed, inject } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Observable } from 'rxjs';

import { ToymodEffects } from './toymod.effects';

describe('ToymodService', () => {
  let actions$: Observable<any>;
  let effects: ToymodEffects;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ToymodEffects,
        provideMockActions(() => actions$)
      ]
    });

    effects = TestBed.get(ToymodEffects);
  });

  it('should be created', () => {
    expect(effects).toBeTruthy();
  });
});
