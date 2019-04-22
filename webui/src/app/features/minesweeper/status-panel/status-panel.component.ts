import {Component, OnDestroy, OnInit} from '@angular/core';
import {Store} from '@ngrx/store';
import {Subscription} from 'rxjs';

import * as fromStore from '../store/reducers/minesweeper.reducer';

@Component({
  selector: 'jchptf-status-panel',
  templateUrl: './status-panel.component.html',
  styleUrls: ['./status-panel.component.css']
})
export class StatusPanelComponent implements OnDestroy
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
