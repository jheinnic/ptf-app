import { Injectable } from '@angular/core';
import { Actions, Effect } from '@ngrx/effects';

@Injectable()
export class LayoutEffects {
  constructor(private actions$: Actions) {}
}
