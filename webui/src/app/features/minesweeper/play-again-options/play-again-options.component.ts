import {Component, OnDestroy} from '@angular/core';
import {Subscription} from 'rxjs';
import {Store} from '@ngrx/store';

import * as fromStore from '../store/reducers/minesweeper.reducer';

@Component({
  selector: 'jchptf-play-again-options',
  templateUrl: './play-again-options.component.html',
  styleUrls: ['./play-again-options.component.css']
})

export class PlayAgainOptionsComponent implements OnDestroy
{
  public safeCellsLeftSubscription: Subscription;

  public safeCellsLeft: number;

  constructor(private store: Store<fromStore.State>)
  {
    // Subscribe to the outcome state and the game state Enum on an ongoing basis
    // to keep those UI details live.
    this.safeCellsLeftSubscription = this.store.select(
      fromStore.selectSafeCellsLeft
    )
      .subscribe(
        (value: number) => {
          this.safeCellsLeft = value;
        }
      );
  }

  ngOnDestroy() {
    this.safeCellsLeftSubscription.unsubscribe();
  }
}
