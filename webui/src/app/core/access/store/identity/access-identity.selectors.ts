import * as Models from './access-identity.models';

type State = Models.State;
type SessionTokenStatus = Models.SessionTokenStatus;
type ActivityContext = Models.ActivityContext;
type HasTokenContext = Models.HasTokenContext;
type LoggedInContext = Models.LoggedInContext;

export const isAuthentClientReady = (state: State) =>
  state && state.authentClientState === Models.AuthentClientStatus.Connected;

export const isUiAvailable = (state: State) => state && state.isUiAvailable;

export const getIdentityErrorMessage = (state: State) => (state ? state.errorDetail.displayMessage : undefined);

export const hasIdentityErrorMessage = (state: State) =>
  state && state.errorDetail.errorCause !== noFailure && state.errorDetail.errorCause !== pending;

export const currentIdentityActivityContext = (state: State) =>
  state ? state.activityContext : undefined;

export const currentIdentityActivityMode = (activityContext: ActivityContext) =>
  activityContext ? activityContext.activityMode : undefined;

export const isAnonymous = (activityMode: Models.ActivityMode) =>
  activityMode === anonymous;

function isSessionTokenContext(context: ActivityContext): context is HasTokenContext
{
  return context && (context.activityMode === loadingProfile || context.activityMode === loggedIn
    || context.activityMode === loggingOut);
}

export const hasSessionToken = (activityContext: ActivityContext) =>
  isSessionTokenContext(activityContext);

export const currentHasTokenActivityContext = (activityContext: ActivityContext, ifHasSessionToken: boolean) =>
  ifHasSessionToken ? activityContext as HasTokenContext : undefined;

// TODO: Collapse this down into the selectors that follow...
export const sessionTokenStatus = (activityContext: HasTokenContext | undefined) =>
  activityContext ? activityContext.tokenStatus : undefined;

export const hasValidLogin = (tokenStatus: SessionTokenStatus) =>
  tokenStatus === valid;

export const hasInvalidLogin = (tokenStatus: SessionTokenStatus) =>
  tokenStatus && tokenStatus !== valid;

export const hasExpiredLogin = (tokenStatus: SessionTokenStatus) =>
  tokenStatus === expired;

export const hasRevokedLogin = (tokenStatus: SessionTokenStatus) =>
  tokenStatus === revoked;

export const hasMalformedLogin = (tokenStatus: SessionTokenStatus) =>
  tokenStatus === malformed;

export const hasUnverifiedLogin = (tokenStatus: SessionTokenStatus) =>
  tokenStatus === unknown;

// Reasoning here goes that if we end up logged out without having transitioned to the "logout" activity
// mode, then we were involuntarily logged out.  E.g. session revocation, expiration, or third party logout.
// export const wasLogoutPlanned =
//   createSelector(activityModeInProgress, activityMode => activityMode === loggingOut);

// TODO: Address retries
export const isUserProfileLoaded = (activityMode: Models.ActivityMode) =>
  activityMode === loggedIn;

export const getUserProfile = (activityContext: HasTokenContext, profileLoaded: boolean) =>
  profileLoaded ? (activityContext as LoggedInContext).userProfile : undefined;

const noFailure = Models.ErrorCause.NoFailure;
const pending = Models.ErrorCause.Pending;

const valid = Models.SessionTokenStatus.Authenticated;
const expired = Models.SessionTokenStatus.Expired;
const revoked = Models.SessionTokenStatus.Revoked;
const malformed = Models.SessionTokenStatus.Malformed;
const unknown = Models.SessionTokenStatus.Unknown;

const anonymous = Models.ActivityMode.Anonymous;
const loggedIn = Models.ActivityMode.LoggedIn;
const loadingProfile = Models.ActivityMode.LoadingProfile;
const loggingOut = Models.ActivityMode.LoggingOut;


