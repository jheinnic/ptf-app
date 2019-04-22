import {IsDefined, IsPositive, IsUUID} from 'class-validator';
import {RevealedCellContent} from './revealed-cell-content.class';
import {PlayerStatus} from './player-status.enum';

export class GameResumedDto
{
  constructor(
    overrides: Partial<GameResumedDto> = { },
    defaults: Partial<GameResumedDto> = GameResumedDto.DEFAULTS
  )
  {
    Object.assign(this, overrides, defaults);
  }

  private static readonly NO_REVELATIONS = [ ] as ReadonlyArray<RevealedCellContent>;

  private static readonly DEFAULTS: Partial<GameResumedDto> = {
    gameBoardId: undefined,
    latestTurnId: 0,
    playerStatus: PlayerStatus.INACTIVE,
    cellsRevealed: GameResumedDto.NO_REVELATIONS,
    safeCellsLeft: 0
  };

  @IsUUID()
  @IsDefined()
  public readonly gameBoardId: string;

  @IsPositive()
  public readonly latestTurnId: number;

  @IsDefined()
  public readonly playerStatus: PlayerStatus;

  @IsDefined()
  public readonly cellsRevealed: ReadonlyArray<RevealedCellContent>;

  @IsPositive()
  public readonly safeCellsLeft: number;

}

export interface IGameResumedDto extends GameResumedDto { }
