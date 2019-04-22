import {Action} from '@ngrx/store';

// export const BOOTSTRAP_AUTHORIZATION = '(Authentication UI) Select Keycloak Client';
export const AUTHORIZATION_READY = '(Authorization Service) Authorization Bootstrap Complete';


// export class BootstrapAuthorization implements Action
// {
//   readonly type = BOOTSTRAP_AUTHORIZATION;
//
//   constructor(public readonly payload: AccessIdentityModels.KeycloakClient) { }
// }

export class AuthorizationReady implements Action
{
  public readonly type = AUTHORIZATION_READY;
}

export type ActionType =
  // BootstrapAuthorization |
  AuthorizationReady;

