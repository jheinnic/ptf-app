import {Inject, Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import {flatMap, map, take} from 'rxjs/operators';

import {MINE_SWEEPER_API_URL} from '../../../../shared/di/environment-di.tokens';
import {IGameCreatedDto} from '../../dto/replies/game-created.dto';
import {IPlayerTurnOutcomeDto} from '../../dto/replies/player-turn-outcome.dto';
import {PlayerStatus} from '../../dto/replies/player-status.enum';
import {ICreateGameRequestDto} from '../../dto/requests/create-game-request.dto';
import {IMakeMoveRequestDto} from '../../dto/requests/make-move-request.dto';

import {
  MinesweeperActionTypes, SendNextMove, ReceiveGameConcluded, ReceiveGameContinues
} from '../actions/minesweeper.actions';
import {SetupOptions} from '../models/minesweeper.models';
import * as fromStore from '../reducers/minesweeper.reducer';

@Injectable()
export class MinesweeperEffects {

  @Effect({dispatch: true})
  beginNewGame$ =
    this.actions$.pipe(
      ofType(MinesweeperActionTypes.SendBeginGame),
      flatMap(
        () => this.store$.select(fromStore.selectSetupOptions).pipe(
          take(1),
          flatMap(
            (setupOptions: SetupOptions) => {
              const requestDto: ICreateGameRequestDto = setupOptions;
              console.log('Sending POST for ', this.apiUrl, requestDto);
              return this.httpClient.post<IGameCreatedDto>(
                this.apiUrl + '/', requestDto, {observe: 'response', responseType: 'json'}
              ).pipe(
                map((response: HttpResponse<IGameCreatedDto>) => {
                  const safeCellsLeft =
                    (setupOptions.xSize * setupOptions.ySize) - setupOptions.mineCount;
                  return { httpResponse: response, safeCellsLeft };
                })
              );
            }
          )
        )
      ),
      map(
        ( {httpResponse, safeCellsLeft}: {httpResponse: HttpResponse<IGameCreatedDto>, safeCellsLeft: number} ) => {
           console.log('Processing response: ', httpResponse);
           return new ReceiveGameContinues({
             afterTurnId: -1,
             nextTurnId: httpResponse.body.nextTurnId,
             safeCellsLeft: safeCellsLeft,
             cellsRevealed: []
           });
        }
      )
    );

  @Effect({dispatch: true})
  playNextMove$ =
    this.actions$.pipe(
      ofType(MinesweeperActionTypes.SendNextMove),
      flatMap((action: SendNextMove) => {
        return this.store$.select(fromStore.selectExpectedTurnId)
          .pipe(
            take(1),
            flatMap((nextTurnId: number) => {
              const request: IMakeMoveRequestDto = {
                turnId: nextTurnId,
                xCell: action.payload.xCell,
                yCell: action.payload.yCell
              };
              console.log('Sending PUT for', this.apiUrl, request);
              return this.httpClient.put<IPlayerTurnOutcomeDto>(
                this.apiUrl + '/', request, {observe: 'response', responseType: 'json'}
              );
            })
          );
      }),
      map((response: HttpResponse<IPlayerTurnOutcomeDto>) => {
        console.log('Received', response);
        /*
        return new ShowRevealedCells(response.body);
      })
  );

  @Effect({dispatch: true})
  showRevealedCells$ = this.actions$.pipe(
    ofType(MinesweeperActionTypes.ShowRevealedCells),
    map((response: ShowRevealedCells) => {
      console.log(response.payload);
      */

      switch (response.body.playerStatus) {
        case PlayerStatus.PLAYING: {
          return new ReceiveGameContinues(response.body);
        }
        case PlayerStatus.DEFEATED: {
          return new ReceiveGameConcluded(response.body);
        }
        case PlayerStatus.WINNER: {
          return new ReceiveGameConcluded(response.body);
        }
        default: {
          console.error('Unexpected status result: ' + response.body.playerStatus);
        }
      }
    })
  );

  constructor(
    private actions$: Actions,
    private store$: Store<fromStore.State>,
    private httpClient: HttpClient,
    @Inject(MINE_SWEEPER_API_URL) private apiUrl: string
  ) { }
}
