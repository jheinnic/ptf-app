import {IsUUID} from 'class-validator';

export class ResumeGameRequestDto {
   @IsUUID()
   public readonly gameBoardId: string;

   constructor(gameBoardId: string) {
      this.gameBoardId = gameBoardId;
   }
}

export interface IResumeGameRequestDto extends ResumeGameRequestDto { }
