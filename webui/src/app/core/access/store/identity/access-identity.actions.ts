import {Action} from '@ngrx/store';
import {KeycloakProfile} from 'keycloak-js';
import * as uuid from 'uuid';

import * as Models from './access-identity.models';
import {OnReturnUrlDetail} from './access-identity.models';

// export const BOOTSTRAP_AUTHENTICATION = '(Authentication UI) Select Keycloak Client';

/*
 * Command actions are marked "Authentication UI" and represent commands issued from the UI.
 *
 * Command actions typically modify some bookkeeping state in the store and may also trigger effects that
 * cause the application to perform an asynchronous action, the result of which will typically be to dispatch
 * Event Actions to "respond" to the outcome.
 *
 * Beware anti-pattern where a Command action triggers effects that dispatch additional commands without
 * use of an asynchronous callback.  Do not create additional actions like this.  Instead, register additional
 * effects and reducers against the existing command.
 */
export const REDIRECT_TO_LOGIN = '(Authentication UI) Request Login';
export const RETURN_FROM_LOGIN = '(Authentication UI) Return From Login';
export const REDIRECT_TO_SIGNUP = '(Authentication UI) Request Signup';
export const RETURN_FROM_SIGNUP = '(Authentication UI) Return From Signup';
export const REDIRECT_TO_LOGOUT = '(Authentication UI) Request Logout';
export const RETURN_FROM_LOGOUT = '(Authentication UI) Return From Logout';

export const RETURN_TO_POST_AUTH_URL = '(Authentication UI) Request on-return redirection';
export const SET_POST_AUTH_URL = '(Authentication UI) Configure on-return redirection';

export const REQUEST_TOKEN_REFRESH = '(Authentication UI) Request Token Refresh';

export const REQUEST_USER_PROFILE = '(Authentication UI) Request User Profile Refresh';

// TODO: This command is likely an anti-pattern currently encapsulating some followup behavior associated
//       with TOKEN_REFRESH_ERROR, FIND_MALFORMED_TOKEN, and USER_PROFILE_ERROR.  Please migrate its behavior
//       to an effect triggered by those genuine actions and remove this one.
export const PURGE_FAILED_SESSION = '(Authentication UI) Purge Session Tokens';

/**
 * Event actions are marked "Authentication Service" and represent commands issued as a consequence of
 * the outcome of an asynchronous action.
 *
 * Event actions may trigger further asynchronous actions that dispatch additional commands during their
 * completion callbacks, but as with Command Actions, dispatching additional actions synchronously from
 * an event action's effect handler is an anti-pattern and should instead be addressed by creating
 * additional effect handlers that react to the original event action itself, as well as any other
 * events meant to trigger the behavior in question.
 */
export const AUTHENTICATION_READY = '(Authentication Service) Authentication Bootstrap Complete';

export const RECEIVE_TOKEN_REFRESH = '(Authentication Service) Token Refresh Failed';
export const TOKEN_REFRESH_ERROR = '(Authentication Service) Token Refresh Failed';

export const RECEIVE_USER_PROFILE = '(Authentication Service) User Profile Fetched';
export const USER_PROFILE_ERROR = '(Authentication Service) User Profile Failed';

export const FIND_NO_TOKEN = '(Authentication Service) No access token';
export const FIND_VALID_TOKEN = '(Authentication Service) Valid access token';
export const FIND_EXPIRED_TOKEN = '(Authentication Service) Expired access token';
export const FIND_REVOKED_TOKEN = '(Authentication Service) Revoked access token';
export const FIND_MALFORMED_TOKEN = '(Authentication Service) Malformed access token';

export const PROCESS_BOOTSTRAP_ERROR = '(Authentication Service) Bootstrap Failed';
export const PROCESS_LOGIN_ERROR = '(Authentication Service) Login Error found';
export const PROCESS_LOGOUT_ERROR = '(Authentication Service) Logout Failed';
export const PROCESS_SIGNUP_ERROR = '(Authentication Service) Signup Failed';


// export class BootstrapAuthenticationClient implements Action
// {
//   public readonly type = BOOTSTRAP_AUTHENTICATION;
//
//   constructor(public readonly payload: AccessIdentityModels.KeycloakClient) { }
// }

export class AuthenticationReady implements Action
{
  public readonly type = AUTHENTICATION_READY;
}

export class BootstrapError implements Action
{
  public readonly type = PROCESS_BOOTSTRAP_ERROR;

  constructor(public readonly payload: Models.ErrorDetail) { }
}

export class RequestLoginRedirect implements Action
{
  public readonly type = REDIRECT_TO_LOGIN;

  public readonly payload?: OnReturnUrlDetail;

  constructor(onReturnRedirectUrl?: string) {
    if (!!onReturnRedirectUrl) {
      this.payload = {
        uuid: uuid.v4(),
        onReturnRedirectUrl: onReturnRedirectUrl
      };
    } else {
      this.payload = undefined;
    }
  }
}

export class ReturnFromLogin implements Action
{
  public readonly type = RETURN_FROM_LOGIN;

  constructor(public readonly payload: Models.OnReturnUrlKey) { }
}

export class RequestLogoutRedirect implements Action
{
  public readonly type = REDIRECT_TO_LOGOUT;

  constructor(public readonly payload: Models.OnReturnUrlValue) { }
}

export class RequestSignupRedirect implements Action
{
  public readonly type = REDIRECT_TO_SIGNUP;


  constructor(public readonly payload: Models.OnReturnUrlValue) { }
}

export class ReturnToPostAuthUrl implements Action
{
  public readonly type = RETURN_TO_POST_AUTH_URL;

  constructor(public readonly payload: Models.OnReturnUrlKey) { }
}

export class SetPostAuthUrl implements Action
{
  public readonly type = SET_POST_AUTH_URL;

  public readonly payload: Models.OnReturnUrlDetail;

  constructor(onReturnRedirectUrl: string)
  {
    this.payload = {
      uuid: uuid.v4(),
      onReturnRedirectUrl
    };
  }
}

export class RequestUserProfile implements Action
{
  public readonly type = REQUEST_USER_PROFILE;

  constructor( ) {
  }
}

export class RequestTokenRefresh implements Action
{
  public readonly type = REQUEST_TOKEN_REFRESH;
}

export class FindSessionLoggedOut implements Action
{
  public readonly type = FIND_NO_TOKEN;
}

export class FindSessionLoggedIn implements Action
{
  public readonly type = FIND_VALID_TOKEN;
}

export class FindSessionExpired implements Action
{
  public readonly type = FIND_EXPIRED_TOKEN;
}

export class FindSessionRevoked implements Action
{
  public readonly type = FIND_REVOKED_TOKEN;
}

export class FindTokenMalformed implements Action
{
  public readonly type = FIND_MALFORMED_TOKEN;
}

export class PurgeFailedSession implements Action
{
  public readonly type = PURGE_FAILED_SESSION;
}

export class ProcessLoginError implements Action
{
  public readonly type = PROCESS_LOGIN_ERROR;

  constructor(public payload: Models.ErrorDetail) { }
}

export class ProcessSignupError implements Action
{
  public readonly type = PROCESS_SIGNUP_ERROR;

  constructor(public payload: Models.ErrorDetail) { }
}

export class ProcessLogoutError implements Action
{
  public readonly type = PROCESS_LOGOUT_ERROR;

  constructor(public payload: Models.ErrorDetail) { }
}

export class ReceiveUserProfile implements Action
{
  public readonly type = RECEIVE_USER_PROFILE;

  constructor(public payload: KeycloakProfile) { }
}

export class UserProfileError implements Action
{
  public readonly type = USER_PROFILE_ERROR;

  constructor(public payload: Models.ErrorDetail) { }
}

export class ReceiveTokenRefresh implements Action
{
  public readonly type = RECEIVE_TOKEN_REFRESH;
}

export class TokenRefreshError implements Action
{
  public readonly type = TOKEN_REFRESH_ERROR;

  constructor(public readonly payload: Models.ErrorDetail) { }
}


export type ActionType =
  AuthenticationReady |
  BootstrapError |
  RequestLoginRedirect |
  RequestLogoutRedirect |
  RequestSignupRedirect |
  ReturnToPostAuthUrl |
  SetPostAuthUrl |
  RequestUserProfile |
  PurgeFailedSession |
  FindSessionLoggedIn |
  FindSessionLoggedOut |
  FindSessionExpired |
  FindSessionRevoked |
  FindTokenMalformed |
  ProcessLoginError |
  ProcessSignupError |
  ProcessLogoutError |
  ReceiveUserProfile |
  UserProfileError |
  ReceiveTokenRefresh |
  TokenRefreshError;

