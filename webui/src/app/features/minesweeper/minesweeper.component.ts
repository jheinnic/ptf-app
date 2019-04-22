import {Component} from '@angular/core';
import {Store} from '@ngrx/store';
import {Observable} from 'rxjs';

import * as fromStore from './store/reducers/minesweeper.reducer';

@Component({
  selector: 'jchptf-minesweeper',
  templateUrl: './minesweeper.component.html',
  styleUrls: ['./minesweeper.component.css']
})
export class MinesweeperComponent
{
  public readonly gameInProgress: Observable<boolean>;

  constructor(private store: Store<fromStore.State>)
  {
    this.gameInProgress = this.store.select(
      fromStore.allSelectors.selectGameInProgress);
  }

  // ngOnInit()
  // {
  //   this.boardStateSubscription = this.boardState.subscribe(
  //     (value: GameBoardCell[]) => {
  //       this.boardStateValue = value;
  //       this.gameInProgress = !(!value || (value.length === 0));
  //     }
  //   );
  // }

  // ngOnDestroy()
  // {
  //   this.boardStateSubscription.unsubscribe();
  // }
}
