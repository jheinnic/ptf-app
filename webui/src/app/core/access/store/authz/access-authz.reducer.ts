import * as Models from './access-authz.models';
import * as Actions from './access-authz.actions';

export * from './access-authz.selectors';

export type State = Models.State;

export const initialState: State = {
  authzFactoryState: Models.AuthorizationFactoryStatus.Disconnected,
  lastAccessCheck: undefined
};

export function reducer(state = initialState, action: Actions.ActionType): State
{
  switch (action.type) {
    case Actions.AUTHORIZATION_READY:
    {
      return {
        ...state,
        authzFactoryState: Models.AuthorizationFactoryStatus.Connected
      };
    }

    default:
    {
      return state;
    }
  }
}

