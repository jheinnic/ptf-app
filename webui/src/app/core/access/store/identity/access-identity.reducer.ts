// import {AccessIdentityModels.AuthentActivityMode, BootstrapErrorType} from './authent-client.models';
// import {AccessTokenStatus, RefreshTokenStatus, UserInfoStatus} from './feature/client/client.model';
import * as Models from './access-identity.models';
import * as Actions from './access-identity.actions';

export * from './access-identity.selectors';

export type State = Models.State;

export const initialState: State = {
  authentClientState: Models.AuthentClientStatus.Disconnected,
  errorDetail: {
    errorCause: Models.ErrorCause.Pending,
    displayMessage: 'Loading...'
  },
  activityContext: {
    activityMode: Models.ActivityMode.Anonymous
  },
  isUiAvailable: false
};

export function reducer(state = initialState, action: Actions.ActionType): State
{
  switch (action.type) {
    // case Actions.BOOTSTRAP_AUTHENTICATION:
    // {
    //   return {
    //     ...state,
    //     selectedClientName: action.payload.adapterName
    //   };
    // }

    case Actions.AUTHENTICATION_READY:
    {
      return {
        ...state,
        authentClientState: Models.AuthentClientStatus.Connected
      };
    }

    case Actions.PROCESS_BOOTSTRAP_ERROR:
    {
      return {
        ...state,
        authentClientState: Models.AuthentClientStatus.Disconnected,
        errorDetail: action.payload,
        isUiAvailable: true
      };
    }

    case Actions.REDIRECT_TO_LOGIN:
    {
      switch (state.activityContext.activityMode) {
        case Models.ActivityMode.Anonymous:
        case Models.ActivityMode.Authenticate:
        case Models.ActivityMode.Signup:
        {
          return {
            ...state,
            activityContext: {
              activityMode: Models.ActivityMode.Authenticate
            },
            errorDetail: {
              errorCause: Models.ErrorCause.Pending,
              displayMessage: 'Please log in...'
            },
            isUiAvailable: false,
          };
        }
        case Models.ActivityMode.LoadingProfile:
        case Models.ActivityMode.LoggedIn:
        case Models.ActivityMode.LoggingOut:
        default:
        {
          return state;
        }
      }
    }

    case Actions.REDIRECT_TO_SIGNUP:
    {
      switch (state.activityContext.activityMode) {
        case Models.ActivityMode.Anonymous:
        case Models.ActivityMode.Authenticate:
        case Models.ActivityMode.Signup:
        {
          return {
            ...state,
            activityContext: {
              activityMode: Models.ActivityMode.Signup
            },
            errorDetail: {
              errorCause: Models.ErrorCause.Pending,
              displayMessage: 'Please register...'
            },
            isUiAvailable: false
          };
        }
        case Models.ActivityMode.LoadingProfile:
        case Models.ActivityMode.LoggedIn:
        case Models.ActivityMode.LoggingOut:
        default:
        {
          return state;
        }
      }
    }

    case Actions.REDIRECT_TO_LOGOUT:
    {
      switch (state.activityContext.activityMode) {
        case Models.ActivityMode.Anonymous:
        case Models.ActivityMode.Authenticate:
        case Models.ActivityMode.Signup:
        {
          return {
            ...state,
            activityContext: {
              activityMode: Models.ActivityMode.Anonymous
            },
            errorDetail: {
              errorCause: Models.ErrorCause.NoFailure
            }
          };
        }
        case Models.ActivityMode.LoadingProfile:
        case Models.ActivityMode.LoggedIn:
        case Models.ActivityMode.LoggingOut:
        {
          return {
            ...state,
            activityContext: {
              activityMode: Models.ActivityMode.LoggingOut,
              tokenStatus: state.activityContext.tokenStatus
            },
            errorDetail: {
              errorCause: Models.ErrorCause.Pending,
              displayMessage: 'Logging out...'
            },
            isUiAvailable: false
          };
        }
        default:
        {
          return state;
        }
      }
    }

    case Actions.REQUEST_USER_PROFILE:
    {
      if (state && state.activityContext.activityMode === Models.ActivityMode.LoadingProfile) {
        return {
          ...state,
          activityContext: {
            ...state.activityContext
          },
          errorDetail: {
            errorCause: Models.ErrorCause.Pending,
            displayMessage: 'Loading user profile...'
          }
        };
      } else {
        if (state) {
          console.error(
            'Can only REQUEST_USER_INFO while in Identify authentication mode.  Current mode is ',
            state.activityContext.activityMode
          );
        } else {
          console.error('Cannot REQUEST_USER_INFO before bootstrapping');
        }

        return state;
      }
    }

    case Actions.FIND_VALID_TOKEN:
    {
      return {
        ...state,
        activityContext: {
          activityMode: Models.ActivityMode.LoadingProfile,
          tokenStatus: Models.SessionTokenStatus.Authenticated
        },
        errorDetail: {
          errorCause: Models.ErrorCause.NoFailure,
          displayMessage: undefined
        },
        isUiAvailable: false
      };
    }

    case Actions.FIND_NO_TOKEN:
    {
      return {
        ...state,
        activityContext: {
          activityMode: Models.ActivityMode.Anonymous
        },
        errorDetail: {
          errorCause: Models.ErrorCause.NoFailure,
          displayMessage: undefined
        },
        isUiAvailable: true
      };
    }

    case Actions.FIND_EXPIRED_TOKEN:
    {
      switch (state.activityContext.activityMode) {
        case Models.ActivityMode.LoadingProfile:
        case Models.ActivityMode.LoggedIn:
        {
          return {
            ...state,
            activityContext: {
              ...state.activityContext,
              tokenStatus: Models.SessionTokenStatus.Expired
            },
            errorDetail: {
              errorCause: Models.ErrorCause.Expiration,
              displayMessage: 'Session expired'
            },
            isUiAvailable: true
          };
        }
        case Models.ActivityMode.LoggingOut:
        {
          // This could plausibly happen.  Continue with the logout without logging a warning, as this
          // is most inline with what a reasonable user would expect to happen.
          return state;
        }
        case Models.ActivityMode.Anonymous:
        case Models.ActivityMode.Authenticate:
        case Models.ActivityMode.Signup:
        default:
        {
          console.warn(
            'Disregarding unexpected session expired action while in a state that has no token: ',
            state.activityContext.activityMode);
          return state;
        }
      }
    }

    case Actions.FIND_REVOKED_TOKEN:
    {
      switch (state.activityContext.activityMode) {
        case Models.ActivityMode.LoadingProfile:
        case Models.ActivityMode.LoggedIn:
        {
          return {
            ...state,
            activityContext: {
              ...state.activityContext,
              tokenStatus: Models.SessionTokenStatus.Revoked
            },
            errorDetail: {
              errorCause: Models.ErrorCause.AccessDenied,
              displayMessage: 'Session revoked'
            },
            isUiAvailable: true
          };
        }
        case Models.ActivityMode.LoggingOut:
        {
          // This could plausibly happen.  Continue with the logout without logging a warning, as this
          // is most inline with what a reasonable user would expect to happen.
          return state;
        }
        case Models.ActivityMode.Anonymous:
        case Models.ActivityMode.Authenticate:
        case Models.ActivityMode.Signup:
        default:
        {
          console.warn(
            'Disregarding unexpected session revocation action while in a state that has no token: ',
            state.activityContext.activityMode);
          return state;
        }
      }
    }

    case Actions.FIND_MALFORMED_TOKEN:
    {
      switch (state.activityContext.activityMode) {
        case Models.ActivityMode.LoadingProfile:
        case Models.ActivityMode.LoggedIn:
        {
          return {
            ...state,
            activityContext: {
              ...state.activityContext,
              tokenStatus: Models.SessionTokenStatus.Malformed
            },
            errorDetail: {
              errorCause: Models.ErrorCause.AccessDenied,
              displayMessage: 'Invalid session token'
            },
            isUiAvailable: true
          };
        }
        case Models.ActivityMode.LoggingOut:
        {
          // This could plausibly happen.  Continue with the logout without logging a warning, as this
          // is most inline with what a reasonable user would expect to happen.
          return state;
        }
        case Models.ActivityMode.Anonymous:
        case Models.ActivityMode.Authenticate:
        case Models.ActivityMode.Signup:
        default:
        {
          console.warn(
            'Disregarding unexpected session malformed action while in a state that has no token: ',
            state.activityContext.activityMode);
          return state;
        }
      }
    }

    case Actions.PURGE_FAILED_SESSION:
    {
      switch (state.activityContext.activityMode) {
        case Models.ActivityMode.LoadingProfile:
        case Models.ActivityMode.LoggedIn:
        case Models.ActivityMode.LoggingOut:
        {
          if (state.activityContext.tokenStatus !== Models.SessionTokenStatus.Authenticated) {
            return {
              ...state,
              activityContext: {
                activityMode: Models.ActivityMode.Anonymous
              },
              isUiAvailable: true
            };
          } else {
            console.warn('Ignoring request to purge valid access token.  Use logout instead.');
            return state;
          }
        }
        case Models.ActivityMode.Anonymous:
        case Models.ActivityMode.Authenticate:
        case Models.ActivityMode.Signup:
        default:
        {
          return state;
        }
      }
    }

    case Actions.RECEIVE_USER_PROFILE:
    {
      switch (state.activityContext.activityMode) {
        case Models.ActivityMode.LoadingProfile:
        case Models.ActivityMode.LoggedIn:
        case Models.ActivityMode.LoggingOut:
        {
          return {
            ...state,
            activityContext: {
              ...state.activityContext,
              activityMode: Models.ActivityMode.LoggedIn,
              userProfile: action.payload
            },
            errorDetail: {
              errorCause: Models.ErrorCause.NoFailure,
              displayMessage: undefined
            },
            isUiAvailable: true
          };
        }
        case Models.ActivityMode.Anonymous:
        case Models.ActivityMode.Authenticate:
        case Models.ActivityMode.Signup:
        default:
        {
          console.warn(
            'Ignoring request to load user profile with no authenticated session present: ',
            state.activityContext.activityMode
          );
          return state;
        }
      }
    }

    case Actions.USER_PROFILE_ERROR:
    {
      if (state.activityContext.activityMode === Models.ActivityMode.LoadingProfile) {
        return {
          ...state,
          errorDetail: action.payload,
          isUiAvailable: true
        };
      } else {
        if (state.activityContext.activityMode === Models.ActivityMode.LoggedIn) {
          console.warn('Failed to refresh user profile data.  Retaining last known state');
        }
        return state;
      }
    }

    case Actions.PROCESS_LOGIN_ERROR:
    {
      return {
        ...state,
        activityContext: {
          activityMode: Models.ActivityMode.Authenticate,
          onReturnUrlKey: undefined
        },
        errorDetail: action.payload,
        isUiAvailable: true
      };
    }

    case Actions.PROCESS_LOGOUT_ERROR:
    {
      return {
        ...state,
        activityContext: {
          activityMode: Models.ActivityMode.LoggingOut,
          tokenStatus: Models.SessionTokenStatus.Unknown,
          onReturnUrlKey: undefined
        },
        errorDetail: action.payload,
        isUiAvailable: true
      };
    }

    case Actions.PROCESS_SIGNUP_ERROR:
    {
      return {
        ...state,
        activityContext: {
          activityMode: Models.ActivityMode.Signup
          // onReturnUrlKey: undefined
        },
        errorDetail: action.payload,
        isUiAvailable: true
      };
    }

    // case Actions.SET_POST_AUTH_URL:
    // {
    //   return {
    //     ...state,
    //     activityContext: {
    //       ...state.activityContext,
    //       onReturnUrlKey: action.payload.uuid
    //     }
    //   }
    // }

    // TODO: Is there any state change?  Yes, these should be be applied to a store
    //       variable that gets overwritten on each successive change until a redirect
    //       is eventually requested, at which time an effect for Redirect commands
    //       should set the current store value aside in a durable location for
    //       consumption by the /session/auth onReturn component's action.
    // TODO: There should probably be two of these--one for login/signup, the other
    //       for logout.  Or perhaps the payload can differentiate enough.
    case Actions.SET_POST_AUTH_URL:
    case Actions.RETURN_TO_POST_AUTH_URL:
    default:
    {
      return state;
    }
  }

  // TODO: Need cases for redirection to and from logout/login!!!!
}
