import {KeycloakProfile} from 'keycloak-js';

export enum AuthentClientStatus
{
  Disconnected, Connected
}

export enum SessionTokenStatus
{
  Authenticated = 'Authenticated',
  Expired = 'Expired',
  Revoked = 'Revoked',
  Malformed = 'Malformed',
  Unknown = 'Unknown'
}

// export interface KeycloakClient {
//   readonly adapterName: string;
// }

export type LoginProviderType = 'google' | 'facebook' | 'bioid';

// export interface OnBootstrapResult {
//   readonly authenticated: boolean;
// }

// TODO: Common AccessIdentityModels?  Flexible or closely coupled evolution?
export enum ErrorCause
{
  NoFailure, Pending, Server, Network, Application, Expiration, TimeOut, Cancelled, AccessDenied, Unknown
}

// TODO: Common AccessIdentityModels?  Flexible or closely coupled evolution?
export interface ErrorDetail
{
  readonly displayMessage?: string;
  readonly errorCause: ErrorCause;
}

// export interface UserInfo
// {
//   readonly uuid: string;
//   readonly userName: string;
//   readonly email: string;
//   readonly emailVerified: boolean;
//   readonly accessBlocked: boolean;
//   readonly firstName: string;
//   readonly lastName: string;
// }

export interface OnReturnUrlDetail
{
  /**
   * A random state nonce that is passed with the data used to later redirect the browser back to this
   * app so that onReturn detail can be looked up without risk of getting state from another user's
   * previous runtime.
   */
  readonly uuid: string;

  /**
   * Return URL supplied by the Guard that generates this request.  See how it gets the URL it supplies
   * from route data.  Be mindful that the return URL must be side-effect free so user has no surprises
   * on return and the feature cannot be abused by resident Mallory.
   *
   * @param {string} onReturnRedirectUrl
   */
  readonly onReturnRedirectUrl: string;
}

export interface OnReturnUrlKey {
  readonly uuid: string;
}

export interface OnReturnUrlValue {
  readonly onReturnRedirectUrl: string;
}

export enum ActivityMode
{
  // Bootstrap = 'Bootstrap',
  Anonymous = 'Anonymous',
  Authenticate = 'Authenticate',
  LoggingOut = 'LoggingOut',
  Signup = 'Signup',
  LoadingProfile = 'LoadingProfile',
  LoggedIn = 'LoggedIn'
}

// export interface BootstrapContext {
//   activityMode: ActivityMode.Bootstrap;
// }

export interface AnonymousContext
{
  activityMode: ActivityMode.Anonymous;
  deniedRoute?: string;
  // errorDetail: ErrorDetail
}

export interface LogoutContext
{
  activityMode: ActivityMode.LoggingOut;
  tokenStatus: SessionTokenStatus;
  onReturnUrlKey?: string;
  // errorDetail: ErrorDetail;
}

export interface RegistrationContext
{
  activityMode: ActivityMode.Signup;
  onReturnUrlKey?: string;
  // errorDetail: ErrorDetail;
}

export interface AuthenticationContext
{
  activityMode: ActivityMode.Authenticate;
  onReturnUrlKey?: string;
  // errorDetail: ErrorDetail;
}

export interface LoadProfileContext
{
  activityMode: ActivityMode.LoadingProfile;
  tokenStatus: SessionTokenStatus;
  // errorDetail: ErrorDetail;
}

export interface LoggedInContext
{
  activityMode: ActivityMode.LoggedIn;
  tokenStatus: SessionTokenStatus;
  userProfile: KeycloakProfile;
  deniedRoute?: string;
  // errorDetail: ErrorDetail;
}

export type ActivityContext =
  // BootstrapContext
  AnonymousContext
  | AuthenticationContext
  | LogoutContext
  | RegistrationContext
  | LoadProfileContext
  | LoggedInContext;

export type HasTokenContext =
  LogoutContext
  | LoadProfileContext
  | LoggedInContext;


export interface State
{
  authentClientState: AuthentClientStatus;
  errorDetail: ErrorDetail;
  activityContext?: ActivityContext;
  isUiAvailable: boolean;
  // selectedClientName?: string;
}
