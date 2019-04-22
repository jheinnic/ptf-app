import { TestBed, inject } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Observable, of } from 'rxjs';

import { CoreEffects } from './app.effects';

describe('CoreEffects', () => {
  const actions$: Observable<any> = of();
  let effects: CoreEffects;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CoreEffects,
        provideMockActions(() => actions$)
      ]
    });

    effects = TestBed.get(CoreEffects);
  });

  it('should be created', () => {
    expect(effects).toBeTruthy();
  });
});
