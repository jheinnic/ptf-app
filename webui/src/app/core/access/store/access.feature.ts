import {ActionReducerMap, createFeatureSelector, createSelector} from '@ngrx/store';

import * as AccessIdentityModels from './identity/access-identity.models';
import * as AccessIdentityActions from './identity/access-identity.actions';
import * as AccessIdentityReducer from './identity/access-identity.reducer';
import {AccessIdentityEffects} from './identity/access-identity.effects';

import * as AccessAuthzModels from './authz/access-authz.models';
import * as AccessAuthzActions from './authz/access-authz.actions';
import * as AccessAuthzReducer from './authz/access-authz.reducer';
import {AccessAuthzEffects} from './authz/access-authz.effects';

export {AccessIdentityModels};
export {AccessIdentityActions};
export {AccessIdentityEffects};

export {AccessAuthzModels};
export {AccessAuthzActions};
export {AccessAuthzEffects};

export namespace AccessFeature
{
  export const featureKey = 'access';

  export interface State
  {
    identity: AccessIdentityReducer.State;
    authz: AccessAuthzReducer.State;
  }

  export const reducerMap: ActionReducerMap<State> = {
    identity: AccessIdentityReducer.reducer,
    authz: AccessAuthzReducer.reducer
  };

  export const initialState: State = {
    identity: AccessIdentityReducer.initialState,
    authz: AccessAuthzReducer.initialState
  };

  export const reducerOptions = {
    initialState: initialState
  };

  const selectCoreFeatureState = createFeatureSelector<State>(featureKey);

  /* Identity Sub-Feature Selectors */

  export const selectFromIdentityState =
    createSelector(selectCoreFeatureState, (state: State) => state.identity);

  export const isAuthentClientReady =
    createSelector(selectFromIdentityState, AccessIdentityReducer.isAuthentClientReady);

  // TODO: Generalize this independently from UI non-availability due to just a pending login.
  //       Note that the reason for UI unavailability due to pending login is an inability to
  //       commit to what set of UI operations are going to be valid after it completes.
  export const isUiAvailable =
    createSelector(selectFromIdentityState, AccessIdentityReducer.isUiAvailable);

  export const hasIdentityErrorMessage =
    createSelector(selectFromIdentityState, AccessIdentityReducer.hasIdentityErrorMessage);

  export const getIdentityErrorMessage =
    createSelector(selectFromIdentityState, AccessIdentityReducer.getIdentityErrorMessage);

  export const currentIdentityActivityContext =
    createSelector(selectFromIdentityState, AccessIdentityReducer.currentIdentityActivityContext);

  export const currentIdentityActivityMode =
    createSelector(currentIdentityActivityContext, AccessIdentityReducer.currentIdentityActivityMode);

  export const isAnonymous =
    createSelector(currentIdentityActivityMode, AccessIdentityReducer.isAnonymous);

  export const hasSessionToken =
    createSelector(currentIdentityActivityContext, AccessIdentityReducer.hasSessionToken);

  export const currentHasTokenActivityContext =
    createSelector(currentIdentityActivityContext, hasSessionToken, AccessIdentityReducer.currentHasTokenActivityContext);

  export const sessionTokenStatus =
    createSelector(currentHasTokenActivityContext, AccessIdentityReducer.sessionTokenStatus);

  export const hasValidLogin =
    createSelector(sessionTokenStatus, AccessIdentityReducer.hasValidLogin);

  export const hasInvalidLogin =
    createSelector(sessionTokenStatus, AccessIdentityReducer.hasInvalidLogin);

  export const hasExpiredLogin =
    createSelector(sessionTokenStatus, AccessIdentityReducer.hasExpiredLogin);

  export const hasRevokedLogin =
    createSelector(sessionTokenStatus, AccessIdentityReducer.hasRevokedLogin);

  export const hasMalformedLogin =
    createSelector(sessionTokenStatus, AccessIdentityReducer.hasMalformedLogin);

  export const hasUnverifiedLogin =
    createSelector(sessionTokenStatus, AccessIdentityReducer.hasUnverifiedLogin);

// Reasoning here goes that if we end up logged out without having transitioned to the "logout" activity
// mode, then we were involuntarily logged out.  E.g. session revocation, expiration, or third party logout.
// export const wasLogoutPlanned =
//   createSelector(activityModeInProgress, AccessIdentityReducer.wasLogoutPlanned);

// TODO: Address retries?
  export const isUserProfileLoaded =
    createSelector(currentIdentityActivityMode, AccessIdentityReducer.isUserProfileLoaded);

  export const getUserProfile =
    createSelector(currentHasTokenActivityContext, isUserProfileLoaded, AccessIdentityReducer.getUserProfile);

  /* Authorization Sub-Feature Selectors */

  export const selectFromAuthzState =
    createSelector(selectCoreFeatureState, (state: State) => state.authz);

  export const isAuthzFactoryReady =
    createSelector(selectFromAuthzState, AccessAuthzReducer.isAuthzFactoryReady);
}

