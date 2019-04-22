import {Action} from '@ngrx/store';
import {IPlayerTurnOutcomeDto} from '../../dto/replies/player-turn-outcome.dto';
import {TurnOutcome} from '../models/minesweeper.models';

export enum MinesweeperActionTypes
{
  SetXSize = '[Minesweeper] Set X Size',
  SetYSize = '[Minesweeper] Set Y Size',
  SetMineCount = '[Minesweeper] Set Mine Count',
  SendBeginGame = '[Minesweeper] Begin Minesweeper game',
  SendNextMove = '[Minesweeper] Send next minesweeper move',
  SendAbortGame = '[Minesweeper] Send abort current game request',
  ReceiveGameContinues = '[Minesweeper] Process move outcome or start of game',
  ReceiveGameConcluded = '[Minesweeper] Process final game event',
  // ShowRevealedCells = '[Minesweeper] Process newly revealed cells',
  // ReportPlayerWins = '[Minesweeper] Process game win result',
}

export class SetXSize implements Action
{
  public readonly type = MinesweeperActionTypes.SetXSize;

  constructor(public readonly payload: number) { }
}

export class SetYSize implements Action
{
  public readonly type = MinesweeperActionTypes.SetYSize;

  constructor(public readonly payload: number) { }
}

export class SetMineCount implements Action
{
  public readonly type = MinesweeperActionTypes.SetMineCount;

  constructor(public readonly payload: number) { }
}

export class SendBeginGame implements Action
{
  public readonly type = MinesweeperActionTypes.SendBeginGame;

  constructor() { }
}

export class SendNextMove implements Action
{
  public readonly type = MinesweeperActionTypes.SendNextMove;

  constructor(public readonly payload: {xCell: number, yCell: number}) { }
}

export class SendAbortGame implements Action
{
  public readonly type = MinesweeperActionTypes.SendAbortGame;

  constructor() { }
}

export class ReceiveGameContinues implements Action
{
  public readonly type = MinesweeperActionTypes.ReceiveGameContinues;

  constructor(public readonly payload: TurnOutcome) { }
}

export class ReceiveGameConcluded implements Action
{
  public readonly type = MinesweeperActionTypes.ReceiveGameConcluded;

  constructor(public readonly payload: TurnOutcome) { }
}

export type MinesweeperActions =
  SetXSize
  | SetYSize
  | SetMineCount
  | SendBeginGame
  | SendNextMove
  | SendAbortGame
  | ReceiveGameContinues
  | ReceiveGameConcluded
  ;

