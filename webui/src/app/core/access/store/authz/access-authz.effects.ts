import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {KeycloakService} from 'keycloak-angular';

import {Actions} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import {NGXLogger} from 'ngx-logger';

import * as Reducer from './access-authz.reducer';

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
export class AccessAuthzEffects
{
  constructor(
    private readonly logService: NGXLogger,
    private readonly keycloakService: KeycloakService,
    private readonly actions$: Actions,
    private readonly store: Store<Reducer.State>,
    private readonly router: Router)
  {
    this.logService.log('AccessAuthzEffects Constructor');
  }
}


