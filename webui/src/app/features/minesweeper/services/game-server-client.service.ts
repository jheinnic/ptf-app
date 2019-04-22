import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {SendBeginGame, SendAbortGame, MinesweeperActions, MinesweeperActionTypes} from '../store/actions/minesweeper.actions';
import {merge, Observable, ObservableInput, Subject} from 'rxjs';
import {Store} from '@ngrx/store';
import {State} from '../store/reducers/minesweeper.reducer';
import {switchMap, takeUntil} from 'rxjs/operators';

@Injectable()
export class GameServerClient {

  public constructor(
    private readonly store: Store<State>,
    private readonly actions: Actions<MinesweeperActions>) {

  }

  private ingress: Subject<MinesweeperActions> = new Subject<MinesweeperActions>();

  @Effect({dispatch: true})
  public readonly commandTap: Observable<MinesweeperActions> =
    this.actions.pipe(
      ofType(MinesweeperActionTypes.SendBeginGame),
      switchMap(
        (_action: SendBeginGame, _index: number): ObservableInput<MinesweeperActions> => {
          return this.ingress.asObservable().pipe(
            takeUntil(
              merge(
                this.actions.pipe(
                  ofType(MinesweeperActionTypes.ReceiveGameConcluded)
                ),
                // this.actions.pipe(
                //   ofType(MinesweeperActionTypes.ReportPlayerWins)
                // ),
                this.actions.pipe(
                  ofType(MinesweeperActionTypes.SendAbortGame)
                )
              )
            )
          );
        }
      )
    );

  cancelGame(): void
  {
    this.ingress.next(
      new SendAbortGame()
    );
  }
}
