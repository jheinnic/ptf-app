import {IsPositive, Max, Min} from 'class-validator';

export class MakeMoveRequestDto {
   @IsPositive()
   @Max(2147483647)
   public readonly turnId: number = 0;

   @Min(0)
   public readonly xCell: number = 0;

   @Min(0)
   public readonly yCell: number = 0;
}

export interface IMakeMoveRequestDto extends MakeMoveRequestDto { }
