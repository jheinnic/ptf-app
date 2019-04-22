import {Component, ElementRef, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Store} from '@ngrx/store';

import * as fromStore from '../store/reducers/minesweeper.reducer';
import {SendNextMove} from '../store/actions/minesweeper.actions';
import {GameBoardCell, GameProgress} from '../store/models/minesweeper.models';
import {Observable, Subscription} from 'rxjs';
import * as util from 'util';

@Component({
  selector: 'jchptf-game-board',
  templateUrl: './game-board.component.html',
  styleUrls: ['./game-board.component.css']
})
export class GameBoardComponent implements OnInit, OnDestroy
{
  private static readonly CELL_SCALES = [24, 32, 48, 64];

  public readonly gameProgress: Observable<GameProgress>;

  @ViewChild('gameBoard', {read: ElementRef}) private _gameBoard: ElementRef<any>;

  private readonly _sizes: Partial<{
    xSize: number,
    containerWidth: number,
    containerOWidth: number,
    idealCellWidth: number,
    idealCellOWidth: number,
    actualCellSize: number
    actualBoardWidth: number;
  }>;

  private storeSubscription: Subscription;

  constructor(private store: Store<fromStore.State>)
  {
    this.gameProgress = this.store.select(fromStore.selectGameProgress);

    this._sizes = {
      actualCellSize: 48,
      actualBoardWidth: 144
    };

    // Stash a one-time copy of the new-game properties that are expected to
    // remain fixed in out lifetime.
    // this.newGameSubscription = this.store.select(
    //   fromStore.selectNewGameProps
    // ).subscribe((value) => {
    //   this.xSize = value.xSize;
    //   this.ySize = value.ySize;
    //   this.mineCount = value.mineCount;
    // });

    // Subscribe to the outcome state and the game state Enum on an ongoing basis
    // to keep those UI details live.
    // this.outcomeSubscription = this.store.select(
    //   fromStore.selectGameProgress
    // ).subscribe(
    //   (value: LatestOutcome) => {
    //     console.log('Replacing last outcome with', JSON.stringify(value));
    //     this.latestOutcome = value;
    //   }
    // );
    // this.gameStateSubscription = this.store.select(
    //   fromStore.selectGameState
    // ).subscribe(
    //   (value: GameState) => {
    //     this.gameState = value;
    //   }
    // );
  }

  public ngOnInit()
  {
    this.storeSubscription = this.gameProgress.subscribe((value: GameProgress) => {
      this._sizes.xSize = value.xSize;
      this.validateBoardSizing();
    });

    // this.store.select(
    //   fromStore.selectNextTurnId
    // );
  }

  public ngOnDestroy() {
    this.storeSubscription.unsubscribe();
  }

  public get cellSize(): number {
    return this._sizes.actualCellSize;
  }

  public get cellFlex(): string {
    return `0 0 ${this.cellSize}px`;
  }

  public get boardWidth(): number {
    return this._sizes.actualBoardWidth;
  }

  private validateBoardSizing() {
    if (!! this._gameBoard) {
      const xCount = this._sizes.xSize;

      this._sizes.containerWidth = this._gameBoard.nativeElement.parent.offsetWidth;
      this._sizes.idealCellWidth = this._gameBoard.nativeElement.parent.offsetWidth / xCount;

      const compatible = GameBoardComponent.CELL_SCALES.filter(
        (cellScale: number) => cellScale <= this._sizes.idealCellWidth );

      if (! compatible.length) {
        this._sizes.actualCellSize = GameBoardComponent.CELL_SCALES[0];
        console.warn(
          `Smallest cell size exceeds overflow threshold by ${GameBoardComponent.CELL_SCALES[0] - this._sizes.idealCellWidth}`);
      } else {
        this._sizes.actualCellSize = compatible.pop();
      }

      this._sizes.actualBoardWidth = this._sizes.actualCellSize * xCount;
      console.log(JSON.stringify(this._sizes));
    }
  }

  public moveHere(xCell: number, yCell: number)
  {
    console.log('Declaring move for x = ', xCell, 'y = ', yCell);
    this.store.dispatch(
      new SendNextMove({ xCell, yCell })
    );
  }

  // cellIcon(cellX: number, cellY: number)
  // {
  //   const cellIndex = this.cellIdx(cellX, cellY);
  //   const cellValue = this.latestOutcome.boardState[cellIndex];
  //   console.log('Revealing icon for: cellX -> ', cellX, 'cellY -> ', cellY, ' = ', cellIndex, ' -> ', cellValue);
  public cellIcon(gameBoardCell: GameBoardCell) {
    console.log(`Revealing ${JSON.stringify(gameBoardCell)}`);
    const cellValue = gameBoardCell.content;

    if (cellValue === -1) {
      return '/assets/tiles/unk.png';
    } else if (cellValue === 0) {
      return '/assets/tiles/blank.png';
    } else if (cellValue === 9) {
      return '/assets/tiles/boom.png';
    } else {
      return `/assets/tiles/0${cellValue}.png`;
    }
  }
}
