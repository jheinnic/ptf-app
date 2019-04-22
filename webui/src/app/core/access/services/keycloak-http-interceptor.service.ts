/// <reference path="keycloak-js/dist/keycloak-authz.d.ts">

import {Inject, Injectable, InjectionToken} from '@angular/core';
import {HttpEvent, HttpHandler, HttpHeaders, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {KeycloakEvent, KeycloakEventType, KeycloakService} from 'keycloak-angular';
import {Store} from '@ngrx/store';
import {interval, Observable} from 'rxjs';
import {concatMap, filter, take} from 'rxjs/operators';
import {LRUMap} from 'lru_map';

import {KeycloakInstance} from 'keycloak-js';
import * as KeycloakAuthorization from 'keycloak-js/dist/keycloak-authz';
import { KeycloakAuthorizationInstance, KeycloakAuthorizationPromise } from 'keycloak-js/dist/keycloak-authz';

import {AccessAuthzActions, AccessFeature} from 'app/core/access/store';
import {API_GATEWAY_URL} from 'app/shared/di';

export const URL_TO_RTP_LRU_CACHE =
  new InjectionToken<LRUMap<string, KeycloakAuthorizationPromise>>(
    'URL_TO_RTP_LRU_CACHE');

/**
 * An Angular http proxy supporting Keycloak auth & authz.
 * Authenticate user, manage tokens and add authorization header to access to remote Keycloak protected
 * resources.
 */
@Injectable()
export class KeycloakHttpInterceptor implements HttpInterceptor
{
  private readonly MAX_UNAUTHORIZED_ATTEMPTS = 2;

  private keycloakAuthzInst: KeycloakAuthorizationInstance;

  private keycloakInst: KeycloakInstance;

  constructor(
    private readonly store: Store<AccessFeature.State>,
    @Inject(API_GATEWAY_URL) private readonly apiGatewayUrl: string,
    private readonly keycloakService: KeycloakService,
    // @Inject(URL_TO_RTP_LRU_CACHE) private readonly lruCache: LRUMap<string,
    // KeycloakAuthorization.KeycloakAuthorizationInstance>)
  )
  {
    // TODO: Why is Authorization Adapter bootstrapped as child of HTTP Interceptor?
    const watchForInit =
      this.keycloakService.keycloakEvents$.pipe(
        filter((event: KeycloakEvent) =>
          event.type === KeycloakEventType.OnReady)
      )
        .subscribe(() => {
          this.keycloakInst = this.keycloakService.getKeycloakInstance();
          this.keycloakAuthzInst = KeycloakAuthorization(this.keycloakInst);
          this.keycloakAuthzInst.init();
          watchForInit.unsubscribe();

          // Official library has no bootstrap notification mechanism, so we rely on
          // periodic observation and a polling interval...
          const subscription = interval(100)
            .pipe(
              filter(() => this.keycloakAuthzInst.config !== undefined),
              take(1)
            )
            .subscribe(
              () => {
                this.store.dispatch(new AccessAuthzActions.AuthorizationReady());
                subscription.unsubscribe();
              }
            );
        });
  }

  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>>
  {
    if (req.url.startsWith(this.apiGatewayUrl)) {
      return this.keycloakService.addTokenToHeader(req.headers)
        .pipe(
          concatMap((headers: HttpHeaders) => {
            const clonedRequest = req.clone({headers});
            console.log('new headers', clonedRequest.headers.keys());
            return next.handle(clonedRequest);
          }));
    } else {
      return next.handle(req);
    }
  }
}
