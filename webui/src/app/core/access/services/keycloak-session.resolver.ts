import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from '@angular/router';
import {Store} from '@ngrx/store';
import {NGXLogger} from 'ngx-logger';
import {Observable} from 'rxjs';
import {filter, take, tap} from 'rxjs/operators';

import {AccessFeature} from '../store';

@Injectable()
export class KeycloakSessionResolver implements Resolve<any>
{
  constructor(
    private readonly store: Store<AccessFeature.State>,
    private readonly logService: NGXLogger
  )
  {
    this.logService.warn('Constructor for KeycloakSessionResolver');
  }

  public resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<any>
  {
    return this.store.select(AccessFeature.isAuthentClientReady).pipe(
      tap((msg) => { console.log('On retainer -- ', msg); }),
      filter(status => status),
      take(1)
    );
  }
}
