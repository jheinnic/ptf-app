import { Injectable } from '@angular/core';
import { Actions, Effect } from '@ngrx/effects';

@Injectable()
export class CoreEffects {
  constructor(private actions$: Actions) {}
}
