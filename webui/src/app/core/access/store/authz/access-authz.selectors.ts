import * as Models from './access-authz.models';

type State = Models.State;

export const isAuthzFactoryReady = (state: State) =>
  state && state.authzFactoryState === Models.AuthorizationFactoryStatus.Connected;

