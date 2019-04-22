import {
  ActivatedRouteSnapshot, CanDeactivate,  RouterStateSnapshot
} from '@angular/router';
import {Injectable} from '@angular/core';
import {Store} from '@ngrx/store';
import {Observable} from 'rxjs';

import {NGXLogger} from 'ngx-logger';
import {HasPostAuthentRoute} from './has-post-authent-route.interface';
import {CoreFeature} from 'app/core/store';
import {AccessIdentityActions} from '../store';

@Injectable()
export class LoginDepartureGuard implements CanDeactivate<HasPostAuthentRoute> {
  constructor(
    private readonly store: Store<CoreFeature.State>,
    private readonly logService: NGXLogger) {
  }

  public canDeactivate(
    component: HasPostAuthentRoute,
    currentRoute: ActivatedRouteSnapshot,
    currentState: RouterStateSnapshot,
    nextState?: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean
  {
    this.store.dispatch(
      new AccessIdentityActions.SetPostAuthUrl(
        // component.getPostAuthUrl()
        currentState.url
      )
    )

    return true;
  }
}
