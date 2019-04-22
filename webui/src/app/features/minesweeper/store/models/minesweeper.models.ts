import {PlayerStatus} from '../../dto/replies/player-status.enum';
import {Omit} from 'simplytyped';

export interface SetupOptions
{
  readonly xSize: number;
  readonly ySize: number;
  readonly mineCount: number;
}

export interface CellCoordinates {
  readonly xCell: number;
  readonly yCell: number;
}

export interface CellIndex {
  readonly id: number;
}

// export type CellLocation = CellCoordinates | CellIndex;

export interface GameBoardCell extends CellCoordinates, Partial<CellIndex>
{
  readonly content: number;
}

export interface GameBoardState
{
  readonly boardContent: ReadonlyArray<GameBoardCell>;
  readonly safeCellsLeft: number;
}

export enum InteractionStateType
{
  INACTIVE,
  THINKING,
  WAITING,
}

export enum PendingOperationType
{
  BEGIN_GAME,
  TURN_OUTCOME,
  ABORT_GAME,
  // RESUME_GAME
}

export interface InactiveInteractionState
{
  readonly type: InteractionStateType.INACTIVE;
  readonly previousGame?: GameOutcome;
}

export interface ThinkingInteractionState
{
  readonly type: InteractionStateType.THINKING;
  readonly nextTurnId: number;
}

export interface WaitingInteractionState
{
  readonly type: InteractionStateType.WAITING;
  readonly expectedTurnId: number;
  readonly operationType: PendingOperationType;
}

export interface WaitingForTurnOutcomeState extends WaitingInteractionState {
  readonly operationType: PendingOperationType.TURN_OUTCOME;
  readonly latestMove: CellCoordinates;
}

export interface WaitingForBeginGameState extends WaitingInteractionState {
  readonly operationType: PendingOperationType.BEGIN_GAME;
}

export interface WaitingForAbortGameState extends WaitingInteractionState {
  readonly operationType: PendingOperationType.ABORT_GAME;
}

export type InteractionState =
  InactiveInteractionState
  | ThinkingInteractionState
  | WaitingForAbortGameState
  | WaitingForBeginGameState
  | WaitingForTurnOutcomeState
  ;


export interface TurnOutcome {
  readonly afterTurnId: number;
  readonly nextTurnId: number;
  readonly safeCellsLeft: number;
  readonly cellsRevealed: ReadonlyArray<GameBoardCell>;
}

export interface GameOutcome {
  readonly xSize: number;
  readonly ySize: number;
  readonly boardContent: ReadonlyArray<GameBoardCell>;
  readonly safeCellsLeft: number;
  readonly latestMove?: CellCoordinates;
}

export interface GameProgress extends GameOutcome {
  readonly interactionStateType: InteractionStateType;
}
