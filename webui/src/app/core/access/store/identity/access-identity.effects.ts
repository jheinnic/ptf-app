import {Inject, Injectable} from '@angular/core';
import {Router, RouterStateSnapshot} from '@angular/router';
import {NGXLogger} from 'ngx-logger';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {RouterCancelPayload} from '@ngrx/router-store';
import {Action} from '@ngrx/store';
import {from, Observable, of, zip, range, defer} from 'rxjs';
import {concatMap, delay, delayWhen, filter, map, retryWhen, takeUntil, tap, onErrorResumeNext} from 'rxjs/operators';
import {KeycloakService, KeycloakEvent, KeycloakEventType} from 'keycloak-angular';
import {KeycloakProfile} from 'keycloak-js';

import * as AccessIdentityActions from './access-identity.actions';
import * as AccessIdentityModels from './access-identity.models';
import {CookieService} from 'ngx-cookie-service';
import {COOKIE_SERVICE} from '../../../core.tokens';
import {ON_RETURN_COOKIE_PATH, ON_RETURN_COOKIE_PREFIX} from '../../access.constants';

/**
 * A thin wrapper around three of the Keycloak service adapter's methods, specifically those that accept
 * input configuration, allowing application-specific settings for those options to be encapsulated.  The
 * encapsulation renders this service sufficiently opaque to be used to create, authenticate, authorize,
 * and terminate the application-scoped identity session from any point within the application where
 * necessary.
 *
 * At present, authorization interface requirements have not yet been addressed.  A future iteration will
 * see this corrected before their absence impairs consumption of this work.
 */
@Injectable()
export class AccessIdentityEffects
{
  constructor(
    logService: NGXLogger,
    private readonly keycloakService: KeycloakService,
    private readonly actions$: Actions,
    // @Inject(COOKIE_SERVICE)
    private readonly cookieService: CookieService,
    private readonly router: Router)
  {
    this.logService = logService;
    this.logService.log('AccessIdentityEffects Constructor');
  }

  private static readonly MAX_PROFILE_RETRIES = 3;

  private static readonly PROFILE_RETRY_DELAY = 520;

  private readonly logService: NGXLogger;

// @Effect({dispatch: false})
// readonly openDb$ : Observable < any > =
//   defer(() => {
//     return this.db.open('app.core');
//   });

  // @Effect({dispatch: false})
  // public bootstrapKeycloakClient$: Observable<Action> = this.actions$.pipe(
  //   ofType(AccessIdentityActions.BOOTSTRAP_AUTHENTICATION),
  //   tap((action: AccessIdentityActions.BootstrapAuthenticationClient) => {
  //     this.keycloakRegistry.bootstrapAuthentication(action.payload.adapterName);
  //     this.logService.info('Just returned from call to async keycloak bootstrap');
  //   })
  // );

  @Effect({dispatch: false})
  public readonly bootstrapFailed$ = this.actions$.pipe(
    ofType(AccessIdentityActions.PROCESS_BOOTSTRAP_ERROR),
    tap(action => {
      this.logService.info('TODO!!!');
    })
  );

// Handlers registered with the Keycloak adapter instance emit actions through following subject to
// trigger responses to external authentication and authorization triggers.
  @Effect()
  private readonly keycloakEvents$: Observable<Action> =
    this.keycloakService.keycloakEvents$.pipe(
      filter((event: KeycloakEvent) => event.type === KeycloakEventType.OnReady),
      map((event: KeycloakEvent) => new AccessIdentityActions.AuthenticationReady())
    );

  /*
  @Effect() private readonly startBootstrapping$: Observable<Action> =
    this.keycloakRegistry.errorEvents
      .flatMap((err: AccessIdentityModels.ErrorDetail) => {
        console.log('Error pipeline will receive ', err);
        return this.currentActivityMode$.map((activityMode: AccessIdentityModels.ActivityMode) => {
          switch (activityMode) {
            case AccessIdentityModels.ActivityMode.Signup:
            {
              return new AccessIdentityActions.ProcessSignupError(err);
            }
            case AccessIdentityModels.ActivityMode.LoggingOut:
            {
              return new AccessIdentityActions.ProcessLogoutError(err);
            }
            case AccessIdentityModels.ActivityMode.Authenticate:
            {
              return new AccessIdentityActions.ProcessLoginError(err)
            }
            case AccessIdentityModels.ActivityMode.LoadingProfile:
            {
              return new AccessIdentityActions.UserProfileError(err);
            }
            case AccessIdentityModels.ActivityMode.Bootstrap:
            {
              return new AccessIdentityActions.BootstrapError(err);
            }
            case AccessIdentityModels.ActivityMode.Anonymous:
            {
              console.log('TODO: Challenge on Anonymous error!?');
              return new AccessIdentityActions.RequestLoginRedirect({
                onReturnRedirectUrl: '/'
              });
            }
            case AccessIdentityModels.ActivityMode.LoggedIn:
            {
              console.log('TODO: Challenge on Authenticated error!?');
              return new AccessIdentityActions.RequestLoginRedirect({
                onReturnRedirectUrl: '/'
              });
            }
            default:
            {
              console.log('Unexpected default code block', activityMode);
              return new AccessIdentityActions.RequestLoginRedirect({
                onReturnRedirectUrl: '/'
              });
            }
          }
        });
      });
  */

  // @Effect({dispatch: false})
  public readonly setPostAuthUrl$ = this.actions$.pipe(
    ofType(AccessIdentityActions.SET_POST_AUTH_URL)
  )
    .subscribe((action: AccessIdentityActions.SetPostAuthUrl) => {
      this.logService.warn(
        `TODO: Save post-auth URL`, action.payload
      );
      // this.db.insert('onReturnUrl', [action.payload])
      //   .pipe(
      //     catchError(err => {
      //       this.logService.warn('Could not retain on-return URL.  Will fall back to default.  ', err);
      //       return of(action.payload);
      //     }),
      //     tap((value) => {
      //       this.logService.info('Output to do', value);
      //       this.logService.info('post auth url action', action);

      // switch (action.payload.activityMode)
      // {
      //   case AccessIdentityModels.ActivityMode.Authenticate:
      //   {
      //     this.keycloakService.login();
      //     break;
      //   }
      //   default:
      //   {
      //     this.logService.error(
      //       'Request to set post-auth return for activity mode ' + action.payload.activityMode);
      //     break;
      //   }
      // }
    });

  // @Effect({dispatch: false})
  public readonly returnToPostAuthUrl$ = this.actions$.pipe(
    ofType(AccessIdentityActions.RETURN_TO_POST_AUTH_URL),
    concatMap((action: AccessIdentityActions.ReturnToPostAuthUrl) => {
      // return this.db.query(
      //   'onReturnUrl',
      //   rec => rec.uuid === action.payload.uuid
      // ).catch(err => {
      //     this.logService.error('Could not query for on return URL', err);
      return of({
        onReturnRedirectUrl: '/'
      })
        .pipe(
          concatMap(
            (rec: { onReturnRedirectUrl: string }) =>
              this.router.navigateByUrl(rec.onReturnRedirectUrl)
                .then((result: boolean) => (
                  {result, ...rec}
                ))
          )
        );
      // });
    })
  )
    .subscribe(
      (result: { result: boolean, onReturnRedirectUrl: string }) => {
        if (result.result) {
          this.logService.debug(`Successful return navigation to ${result.onReturnRedirectUrl}`);
        } else {
          this.logService.warn(`Failed return navigation to ${result.onReturnRedirectUrl}`);
        }
      },
      (err: any) => {
        this.logService.error('Unable to navigate on return', err);
      }
    );

  // @Effect()
  public readonly requestBeginLogin$ = this.actions$.pipe(
    ofType(AccessIdentityActions.REDIRECT_TO_LOGIN),
    map((action: AccessIdentityActions.RequestLoginRedirect) => {
      let returnUri = '/session/auth'
      if (!! action.payload) {
        this.cookieService.deleteAll(ON_RETURN_COOKIE_PATH);
        this.cookieService.set(
          `ON_RETURN_COOKIE_PREFIX${action.payload.uuid}`,
          action.payload.onReturnRedirectUrl,
          new Date().getDate() + 600000,
          ON_RETURN_COOKIE_PATH,
          undefined,
          true,
          'Strict'
        );

        returnUri = `${returnUri}/${action.payload.uuid}`;
      }

      return returnUri;
    }),
    concatMap((redirectUri: string) => this.keycloakService.login({
      redirectUri: redirectUri
    }))
  ).subscribe(
    () => { this.logService.info('Successful redirect to login'); },
    (err: any) => { this.logService.error('Failed redirect to login', err); }
  );

    // map((action: AccessIdentityActions.RequestLoginRedirect) =>
    //   new AccessIdentityActions.SetPostAuthUrl(
    //     action.payload.onReturnRedirectUrl, AccessIdentityModels.ActivityMode.Authenticate)));

// If the on-return handling stumbles on an error, it will get fired from the KeycloakError observable,
// so the only needs to test for the successful case that leads to an on-return redirect.
  @Effect()
  public readonly returnFromLogin$ = this.actions$.pipe(
    ofType(AccessIdentityActions.RETURN_FROM_LOGIN),
    map((action: AccessIdentityActions.ReturnFromLogin) => {
      this.logService.info('In return from login effect');
      return new AccessIdentityActions.ReturnToPostAuthUrl(action.payload);
    }),
    delayWhen(action => this.actions$.pipe(
      ofType(AccessIdentityActions.RECEIVE_USER_PROFILE))
    )
  );

// , tap(value => {
//   this.logService.info('Observed isUserProfileLoaded = ', value);
// })
// }), tap( value => { this.logService.info('Outside delay observed ', value); });

  // @Effect()
  // public onFindValidSession$ = this.actions$.pipe(
  //   ofType(AccessIdentityActions.FIND_VALID_TOKEN),
  //   map(() => new AccessIdentityActions.RequestUserProfile())
  // );

  // @Effect({dispatch: false})
  public loginFailed$ = this.actions$.pipe(
    ofType(AccessIdentityActions.PROCESS_LOGIN_ERROR),
  )
    .subscribe((action: AccessIdentityActions.ProcessLoginError) => {
        this.logService.info('TODO!!!');
        // TODO: This looks right--reenable?  No, not right...
        // this.router.navigate(['session', 'error', {message: 'Login Failure Message TODO'}])
      }
    );

  @Effect()
  public requestUserProfile$ = this.actions$.pipe(
    ofType(
      AccessIdentityActions.REQUEST_USER_PROFILE,
      AccessIdentityActions.FIND_VALID_TOKEN,
      AccessIdentityActions.RETURN_FROM_LOGIN,
    ),
    concatMap(action => {
      this.logService.info(`Load user profile is being requested`);

      return from(
        this.keycloakService.loadUserProfile()
      )
        .pipe(
          retryWhen((errors: Observable<any>) => {
            return zip(
              errors, range(1, AccessIdentityEffects.MAX_PROFILE_RETRIES)
            )
              .pipe(
                delay(AccessIdentityEffects.PROFILE_RETRY_DELAY),
              );
          }),
          map((profile: KeycloakProfile) =>
            new AccessIdentityActions.ReceiveUserProfile(profile)),
          onErrorResumeNext(
            defer(() => of(
              new AccessIdentityActions.UserProfileError({
                errorCause: AccessIdentityModels.ErrorCause.Server,
                displayMessage: 'Too many retries'
              })
            ))
          ),
          takeUntil(
            this.actions$.pipe(
              ofType(
                AccessIdentityActions.REDIRECT_TO_LOGOUT,
                AccessIdentityActions.PURGE_FAILED_SESSION,
              )
            )
          ),
        );
    })
  );

  // @Effect({dispatch: false})
  // public requestLogout$ = this.actions$
  //   .pipe(
  //     ofType(AccessIdentityActions.REDIRECT_TO_LOGOUT),
  //     map((action: AccessIdentityActions.RequestLogoutRedirect) =>
  //       new AccessIdentityActions.SetPostAuthUrl(
  //         action.payload.onReturnRedirectUrl, AccessIdentityModels.ActivityMode.LoggingOut)));

  // @Effect({dispatch: false})
  // public requestRegistration$ = this.actions$.pipe(
  //   ofType(AccessIdentityActions.REDIRECT_TO_SIGNUP),
  //   map((action: AccessIdentityActions.RequestSignupRedirect) =>
  //     new AccessIdentityActions.SetPostAuthUrl(
  //       action.payload.onReturnRedirectUrl, AccessIdentityModels.ActivityMode.Signup)));

  @Effect()
  public requestTokenRefreshObs$ =
    this.actions$.pipe(
      ofType(AccessIdentityActions.REQUEST_TOKEN_REFRESH),
      concatMap(
        (action: AccessIdentityActions.RequestTokenRefresh) =>
          from(
            this.keycloakService.updateToken(60)
              .then(() =>
                new AccessIdentityActions.ReceiveTokenRefresh()
              )
              .catch((err: any) =>
                new AccessIdentityActions.TokenRefreshError({
                  errorCause: AccessIdentityModels.ErrorCause.Unknown,
                  displayMessage: err
                })
              )
          )
            .pipe(
              takeUntil(
                this.actions$.pipe(
                  ofType(
                    AccessIdentityActions.REDIRECT_TO_LOGOUT,
                    AccessIdentityActions.PURGE_FAILED_SESSION,
                  )
                )
              ),
            )
      )
    );

  @Effect({dispatch: false})
  public logoutFailed$ = this.actions$.pipe(
    ofType(AccessIdentityActions.PROCESS_LOGOUT_ERROR),
    tap((action: AccessIdentityActions.ProcessLogoutError) => {
      // this.router.navigate(['session', 'error', {message: 'Logout Failure Message TODO'}]);
    })
  );

  @Effect({dispatch: false})
  public registrationFailed$ = this.actions$.pipe(
    ofType(AccessIdentityActions.PROCESS_SIGNUP_ERROR),
    tap((action: AccessIdentityActions.ProcessSignupError) => {
      // this.router.navigate(['session', 'error', {message: 'Registration Failure Message TODO'}]);
    })
  );

  private static getReturnRedirectUrl(payload: RouterCancelPayload<RouterStateSnapshot, any>): string
  {
    const altRedirect = payload.routerState.data['authGuardRedirect'];

    return altRedirect || '/session/auth';
  }
}
