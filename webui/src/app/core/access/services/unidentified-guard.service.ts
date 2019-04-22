import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router} from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, of } from 'rxjs';
import { map, delayWhen, filter } from 'rxjs/operators';
import {AccessFeature} from '../store';

@Injectable()
export class UnidentifiedGuard implements CanActivate
{
  constructor(private readonly store: Store<AccessFeature.State>, private readonly router: Router) {}

  public canActivate(routeSnapshot: ActivatedRouteSnapshot): Observable<boolean>
  {
    // TODO: This contract cannot be preserved as-is without "RequireAnonymity".  This is also not
    //       currently used, but do not be surprised if it behaves strangely if that should change
    //       later on without implementing mandatory anonymity!
    // this.store.dispatch(new AuthentClientActions.RequireAnonymity());
    return this.store.select(AccessFeature.hasValidLogin).pipe(
      delayWhen(
        of,
        this.store.select(AccessFeature.isAuthentClientReady).pipe(
          filter(value => value))
      ),
      map(invertResult => ! invertResult));
  }
}
