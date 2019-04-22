import {createFeatureSelector, createSelector} from '@ngrx/store';
import * as ndarray from 'ndarray';
import * as util from 'util';

import {InvalidStateChangeError} from '../models/invalid-state-change.error';
import {MinesweeperActions, MinesweeperActionTypes} from '../actions/minesweeper.actions';
import {
  GameBoardCell, GameBoardState, GameProgress, InteractionState, InteractionStateType, PendingOperationType, SetupOptions
} from '../models/minesweeper.models';

const BLANK_INVALID_BOARD: ReadonlyArray<GameBoardCell> = [];

const DEFAULT_SETUP_OPTIONS: SetupOptions = {
  xSize: 3,
  ySize: 3,
  mineCount: 3
};
const DEFAULT_INITIAL_BOARD: ReadonlyArray<GameBoardCell> =
  reallocateBoardContent([], DEFAULT_SETUP_OPTIONS);

export interface State
{
  readonly setupOptions: SetupOptions;
  readonly initialBoard: ReadonlyArray<GameBoardCell>;
  readonly gameBoardState?: GameBoardState;
  readonly interactionState: InteractionState;
}

export const initialState: State = {
  setupOptions: DEFAULT_SETUP_OPTIONS,
  initialBoard: DEFAULT_INITIAL_BOARD,
  interactionState: {
    type: InteractionStateType.INACTIVE,
  },
  gameBoardState: undefined
};

function areBoardOptionsValid(state: SetupOptions)
{
  const {xSize, ySize, mineCount} = state;

  return (
    (xSize >= 3) && (ySize >= 3)) && (mineCount > 0) && (mineCount < (xSize * ySize)
  );
}

function areBoardOptionsMutable(state: State) {
  return state.interactionState.type === InteractionStateType.INACTIVE;
}

function reduceCellRevelations(
  gameBoardContent: ReadonlyArray<GameBoardCell>,
  setupOptions: SetupOptions,
  cellsRevealed: ReadonlyArray<GameBoardCell>)
{
  const boardContent = [...gameBoardContent];
  const ndBoardState: ndarray<GameBoardCell> =
    ndarray<GameBoardCell>(boardContent, [setupOptions.xSize, setupOptions.ySize]);
  let revealedCell: GameBoardCell;
  for (revealedCell of cellsRevealed) {
    // Bypass ndarray.set() since it's type signature incorrectly
    // tolerates only numbers.  ndarray.index() incorrectly types its
    // return as <T>, but it is truly always an integer.
    const stateIndex: number =
      ndBoardState.index(
        revealedCell.xCell, revealedCell.yCell
      ) as unknown as number;

    boardContent[stateIndex] = {
      ...ndBoardState.get(revealedCell.xCell, revealedCell.yCell),
      content: revealedCell.content
    };

    console.log(
      `Revealed ${util.inspect(revealedCell, true, 10, true)} at ${stateIndex}`);
  }

  return boardContent;
}

function reallocateBoardContent(
  oldBoardContent: ReadonlyArray<GameBoardCell>, setupOptions: SetupOptions): ReadonlyArray<GameBoardCell>
{
  if (! areBoardOptionsValid(setupOptions)) {
    return BLANK_INVALID_BOARD;
  }

  const boardSize = setupOptions.xSize * setupOptions.ySize;
  const oldBoardSize = oldBoardContent.length;

  const boardContent = new Array(boardSize);
  const noOldCell = {
    xCell: -1, yCell: -1, content: -99
  };
  for (let ii = 0, xx = 0; ii < boardSize; xx++)
  {
    for (let yy = 0; yy < setupOptions.ySize; yy++, ii++) {
      const { xCell, yCell, content } = oldBoardContent[ii]
        ? oldBoardContent[ii]
        : noOldCell;

      boardContent[ii] = ((xCell === xx) && (yCell === yy) && (content === -1))
        ? oldBoardContent[ii]
        : {
          id: ii,
          xCell: xx,
          yCell: yy,
          content: -1
        };
    }
  }

  return boardContent;
}

export function reducer(state = initialState, action: MinesweeperActions): State
{
  let {setupOptions, interactionState, gameBoardState} = state;

  switch (action.type) {
    case MinesweeperActionTypes.SetXSize:
    {
      if (! areBoardOptionsMutable(state)) {
        throw new InvalidStateChangeError('Can only change board options when a game is not in progress!');
      }

      if (setupOptions.xSize === action.payload) {
        return state;
      }

      setupOptions = { ...setupOptions, xSize: action.payload };
      const initialBoard = reallocateBoardContent(state.initialBoard, setupOptions);

      return { ...state, setupOptions, initialBoard };
    }

    case MinesweeperActionTypes.SetYSize:
    {
      if (! areBoardOptionsMutable(state)) {
        throw new InvalidStateChangeError('Can only change board options when a game is not in progress!');
      }

      if (setupOptions.ySize === action.payload) {
        return state;
      }

      setupOptions = { ...setupOptions, ySize: action.payload };
      const initialBoard = reallocateBoardContent(state.initialBoard, setupOptions);

      return { ...state, setupOptions, initialBoard };
    }

    case MinesweeperActionTypes.SetMineCount:
    {
      if (! areBoardOptionsMutable(state)) {
        throw new InvalidStateChangeError('Can only change board options when a game is not in progress!');
      }

      if (setupOptions.mineCount === action.payload) {
        return state;
      }

      setupOptions = { ...setupOptions, mineCount: action.payload };

      return { ...state, setupOptions };
    }

    case MinesweeperActionTypes.SendBeginGame:
    {
      if (interactionState.type !== InteractionStateType.INACTIVE) {
        throw new InvalidStateChangeError('Cannot begin a game unless no game is in progress');
      } else if (state.initialBoard === BLANK_INVALID_BOARD) {
        throw new InvalidStateChangeError('Cannot begin a game unless board options are valid');
      }

      interactionState = {
        type: InteractionStateType.WAITING,
        expectedTurnId: -1,
        operationType: PendingOperationType.BEGIN_GAME
      };

      return { ...state, interactionState };
    }

    case MinesweeperActionTypes.SendNextMove:
    {
      if (interactionState.type !== InteractionStateType.THINKING) {
        throw new InvalidStateChangeError('Cannot make a move outside of player\'s turn.');
      }

      interactionState = {
        type: InteractionStateType.WAITING,
        operationType: PendingOperationType.TURN_OUTCOME,
        expectedTurnId: interactionState.nextTurnId,
        latestMove: action.payload,
      };

      return {
        ...state,
        interactionState
      };
    }

    case MinesweeperActionTypes.ReceiveGameContinues:
    {
      // TODO: When supporting canceling a game, tolerate receiving a belated result from cancelled game.
      if ((interactionState.type !== InteractionStateType.WAITING) ||
        (
          (interactionState.operationType !== PendingOperationType.TURN_OUTCOME) &&
          (interactionState.operationType !== PendingOperationType.BEGIN_GAME)
        )
      ) {
        throw new InvalidStateChangeError('Unexpected continue game outcome received');
      } else if (interactionState.expectedTurnId !== action.payload.afterTurnId) {
        throw new InvalidStateChangeError(
          `Outcome for turn id ${action.payload.afterTurnId} does not match expected turn id, ${interactionState.expectedTurnId}`);
      }

      const safeCellsLeft = action.payload.safeCellsLeft;
      const boardContent =
        (interactionState.operationType === PendingOperationType.TURN_OUTCOME)
          ? reduceCellRevelations(gameBoardState.boardContent, setupOptions, action.payload.cellsRevealed)
          : gameBoardState.boardContent;

      gameBoardState = {
        ...gameBoardState,
        safeCellsLeft,
        boardContent
      };
      interactionState = {
        type: InteractionStateType.THINKING,
        nextTurnId: action.payload.nextTurnId
      };

      return {
        ...state,
        gameBoardState,
        interactionState
      };
    }

    case MinesweeperActionTypes.ReceiveGameConcluded:
    {
      if ((interactionState.type !== InteractionStateType.WAITING) ||
        (
          (interactionState.operationType !== PendingOperationType.TURN_OUTCOME) &&
          (interactionState.operationType !== PendingOperationType.ABORT_GAME)
        )
      ) {
        throw new InvalidStateChangeError('Unexpected continue game outcome received');
      } else if (interactionState.expectedTurnId !== action.payload.afterTurnId) {
        throw new InvalidStateChangeError(
          `Outcome for turn id ${action.payload.afterTurnId} does not match expected turn id, ${interactionState.expectedTurnId}`);
      }

      let previousGame;
      if (!! gameBoardState) {
        const {xSize, ySize} = setupOptions;
        const latestMove =
          (interactionState.operationType === PendingOperationType.TURN_OUTCOME)
            ? interactionState.latestMove : undefined;
        const safeCellsLeft = action.payload.safeCellsLeft;
        const boardContent = reduceCellRevelations(
          gameBoardState.boardContent, setupOptions, action.payload.cellsRevealed);

        previousGame = { xSize, ySize, boardContent, safeCellsLeft, latestMove };
      } else {
        const {xSize, ySize} = setupOptions;
        const boardContent = action.payload.cellsRevealed;
        const safeCellsLeft = action.payload.safeCellsLeft;

        previousGame = { xSize, ySize, boardContent, safeCellsLeft };
      }

      interactionState = {
        type: InteractionStateType.INACTIVE,
        previousGame
      };

      return { ...state, gameBoardState, interactionState };
    }

    case MinesweeperActionTypes.SendAbortGame:
    {
      if (interactionState.type === InteractionStateType.INACTIVE) {
        throw new InvalidStateChangeError('Can only end a game when there is a game to end.');
      } else if (interactionState.type !== InteractionStateType.THINKING) {
        throw new InvalidStateChangeError('Please wait for pending network activity to abate.');
      }

      interactionState = {
        type: InteractionStateType.WAITING,
        operationType: PendingOperationType.ABORT_GAME,
        expectedTurnId: interactionState.nextTurnId
      };

      return {
        ...state,
        interactionState
      };
    }

    default:
      return state;
  }
}

export const featureKey = 'minesweeper';

export const selectMinesweeperState = createFeatureSelector<State>(featureKey);

export const selectSetupOptions = createSelector(
  selectMinesweeperState, (state: State) => state.setupOptions
);

export const selectInteractionState = createSelector(
  selectMinesweeperState, (state: State) => state.interactionState
);
export const selectNextTurnId = createSelector(
  selectInteractionState, (interactionState: InteractionState) => {
    if (interactionState.type === InteractionStateType.THINKING) {
      return interactionState.nextTurnId;
    }
  });
export const selectExpectedTurnId = createSelector(
  selectInteractionState, (interactionState: InteractionState) => {
    if (interactionState.type === InteractionStateType.WAITING) {
      return interactionState.expectedTurnId;
    }
  });

export const selectGameBoardState = createSelector(
  selectMinesweeperState, (state: State) => state.gameBoardState);

export const selectInitialBoardState = createSelector(
  selectMinesweeperState, (state: State) => state.initialBoard);

export const selectGameProgress = createSelector(
  [selectSetupOptions, selectInitialBoardState, selectGameBoardState, selectInteractionState],
  function (
    setupOptions: SetupOptions,
    initialBoard: ReadonlyArray<GameBoardCell>,
    gameBoardState: GameBoardState,
    interactionState: InteractionState): GameProgress
  {
    const interactionStateType = interactionState.type;

    if (!! gameBoardState) {
      const {xSize, ySize} = setupOptions;
      const {boardContent, safeCellsLeft} = gameBoardState;

      let latestMove;
      if ((interactionState.type === InteractionStateType.WAITING)
        && (interactionState.operationType === PendingOperationType.TURN_OUTCOME)) {
        latestMove = interactionState.latestMove;
      }

      return {
        xSize,
        ySize,
        boardContent,
        safeCellsLeft,
        latestMove,
        interactionStateType
      };
    } else if (interactionState.type === InteractionStateType.INACTIVE) {
      return {
        ...interactionState.previousGame,
        interactionStateType
      };
    } else {
      const {xSize, ySize} = setupOptions;
      const boardContent = initialBoard;
      const safeCellsLeft = initialBoard.length - setupOptions.mineCount;

      return {
        xSize,
        ySize,
        boardContent,
        safeCellsLeft,
        interactionStateType
      };
    }
  });

export const selectSafeCellsLeft = createSelector(
  selectGameProgress, function(progress: GameProgress): number {
    return progress.safeCellsLeft;
  }
);

export const selectPlayerHasTurn = createSelector(
  selectInteractionState,
  function (state: InteractionState): boolean {
    return state.type === InteractionStateType.THINKING;
  }
);

export const selectPlayerMayStartGame = createSelector(
  [selectInteractionState, selectSetupOptions],
  function (interactionState: InteractionState, setupOptions: SetupOptions): boolean
  {
    return (interactionState.type === InteractionStateType.INACTIVE)
      && areBoardOptionsValid(setupOptions);
  }
);

export const selectWaitingOnServer = createSelector(
  selectInteractionState,
  function (state: InteractionState): boolean {
    return state.type === InteractionStateType.WAITING;
  }
);

export const selectGameInProgress = createSelector(
  selectInteractionState,
  function (interactionState: InteractionState) {
    return interactionState.type !== InteractionStateType.INACTIVE;
  }
);

export const allSelectors = {
  selectSetupOptions: selectSetupOptions,
  selectNextTurnId: selectNextTurnId,
  selectExpectedTurnId: selectExpectedTurnId,
  selectGameProgress: selectGameProgress,
  selectSafeCellsLeft: selectSafeCellsLeft,
  selectPlayerHasTurn: selectPlayerHasTurn,
  selectPlayerMayStartGame: selectPlayerMayStartGame,
  selectWaitingOnServer: selectWaitingOnServer,
  selectGameInProgress: selectGameInProgress
};
