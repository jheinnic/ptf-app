import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {Injectable} from '@angular/core';
import {Store} from '@ngrx/store';
import {KeycloakService, KeycloakAuthGuard} from 'keycloak-angular';

import {AccessFeature, AccessIdentityActions} from '../store';

@Injectable()
export class AuthenticatedGuard extends KeycloakAuthGuard {
  constructor(
    protected router: Router,
    protected keycloakAngular: KeycloakService,
    private readonly store: Store<AccessFeature.State>) {
    super(router, keycloakAngular);
  }

  public isAccessAllowed(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
    return new Promise((resolve, reject) => {
      if (!this.authenticated) {
        this.store.dispatch(
          new AccessIdentityActions.RequestLoginRedirect(state.url)
        );

        resolve(false);
      } else {
        const requiredRoles = route.data.roles;
        if (!requiredRoles || requiredRoles.length === 0) {
          return resolve(true);
        } else {
          if (!this.roles || this.roles.length === 0) {
            resolve(false);
          }
          let granted = true;
          for (const requiredRole of requiredRoles) {
            if (this.roles.indexOf(requiredRole) > -1) {
              granted = false;
              break;
            }
          }
          resolve(granted);
        }
      }
    });
  }

  // public canActivate(routeSnapshot: ActivatedRouteSnapshot): Observable<boolean> {
  //   return zip(
  //     this.store.select(AccessFeature.hasValidLogin),
  //     this.store.select(AccessFeature.isAuthentClientReady)
  //   ).pipe(
  //     filter((value: [boolean, boolean]) => value[0] && value[1] ),
  //     map((value: [boolean, boolean]) => true),
  //     publishBehavior(true)
  //   );
  // }
}
