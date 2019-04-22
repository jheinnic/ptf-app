import { Action } from '@ngrx/store';

export namespace ToymodActions {
  export enum ActionTypes {
    LoadToymods = '[Toymod] Load Toymods'
  }

  export class LoadToymods implements Action {
    readonly type = ActionTypes.LoadToymods;
  }

  export type Actions = LoadToymods;
}
