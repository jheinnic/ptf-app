import {IsPositive, IsUUID, Max} from 'class-validator';
import {RevealedCellContent} from './revealed-cell-content.class';

export class GameCreatedDto
{
  constructor(
    overrides: Partial<GameCreatedDto> = { },
    defaults: Partial<GameCreatedDto> = GameCreatedDto.DEFAULTS
  )
  {
    Object.assign(this, overrides, defaults);
  }

  private static readonly NO_REVELATIONS = [ ] as ReadonlyArray<RevealedCellContent>;

  private static readonly DEFAULTS: Partial<GameCreatedDto> = {
    gameBoardId: undefined,
    nextTurnId: 0
  };
  @IsUUID()
  public readonly gameBoardId: string;

  @IsPositive()
  @Max(2147483647)
  public readonly nextTurnId: number;
}

export interface IGameCreatedDto extends GameCreatedDto {}
