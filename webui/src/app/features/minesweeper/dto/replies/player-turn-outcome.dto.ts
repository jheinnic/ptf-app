import {IsDefined, Max, Min} from 'class-validator';
import {RevealedCellContent} from './revealed-cell-content.class';
import {PlayerStatus} from './player-status.enum';

export class PlayerTurnOutcomeDto
{
   constructor(
      overrides: Partial<PlayerTurnOutcomeDto> = { },
      defaults: Partial<PlayerTurnOutcomeDto> = PlayerTurnOutcomeDto.DEFAULTS
   )
   {
      Object.assign(this, overrides, defaults);
   }

   private static readonly NO_REVELATIONS = [ ] as ReadonlyArray<RevealedCellContent>;

   private static readonly DEFAULTS: Partial<PlayerTurnOutcomeDto> = {
     afterTurnId: 1,
     nextTurnId: 0,
     playerStatus: PlayerStatus.INACTIVE,
     cellsRevealed: PlayerTurnOutcomeDto.NO_REVELATIONS,
     safeCellsLeft: 0
   };

   @Min(0)
   @Max(2147483647)
   public readonly afterTurnId: number;

   @Min(0)
   @Max(2147483647)
   public readonly nextTurnId: number;

   @IsDefined()
   public readonly playerStatus: PlayerStatus;

   @IsDefined()
   public readonly cellsRevealed: ReadonlyArray<RevealedCellContent>;

   @Min(0)
   public readonly safeCellsLeft: number;
}

export interface IPlayerTurnOutcomeDto extends PlayerTurnOutcomeDto { }
