import {ToymodActions} from '../actions';
import {ToymodModels} from '../models';

export namespace ToymodSubFeature {
  export type State = ToymodModels.State;

  export const initialState: State = {};

  export function reducer(state: State = initialState, action: ToymodActions.Actions): State {
    switch (action.type) {

      case ToymodActions.ActionTypes.LoadToymods:
        return state;


      default:
        return state;
    }
  }
}
