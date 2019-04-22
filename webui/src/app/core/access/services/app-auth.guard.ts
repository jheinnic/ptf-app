import {inject, Inject, Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {Observable} from 'rxjs';
import {KeycloakService} from 'keycloak-angular';

import {keycloakLoginOptions} from '../../../shared/di/keycloak-di.tokens';

@Injectable({
  providedIn: 'root',
  useFactory: () => new AppAuthGuard(
    inject(Router), inject(KeycloakService), inject(keycloakLoginOptions)
  )
})
export class AppAuthGuard implements CanActivate {
  constructor(
    protected router: Router, protected keycloakAngular: KeycloakService, @Inject(keycloakLoginOptions) protected loginOptions
  ) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean
  {
      // console.log('Is expired says ', this.keycloakAngular.isTokenExpired(100));
    return this.keycloakAngular.isLoggedIn()
      .then((value) => {
        if (!value) {
          this.keycloakAngular.clearToken();
          this.keycloakAngular.login(this.loginOptions);
          return Promise.resolve(false);
        } else {
          let granted = false;
          const requiredRoles = route.data.roles;
          if (!requiredRoles || requiredRoles.length === 0) {
            granted = true;
          } else {
            const userRoles = this.keycloakAngular.getUserRoles();
            if ((!userRoles) || (userRoles.length === 0)) {
              granted = false;
            } else {
              for (const requiredRole of requiredRoles) {
                if (this.keycloakAngular.isUserInRole(requiredRole)) {
                  granted = true;
                  break;
                }
              }
            }
          }

          return Promise.resolve(granted);
        }
      });
  }
}
