import {Injectable} from '@angular/core';
import {Actions} from '@ngrx/effects';
import {NGXLogger} from 'ngx-logger';

/**
 */
@Injectable()
export class ContactsEffects
{
  constructor(
    private readonly logService: NGXLogger,
    private readonly actions$: Actions)
  {
    this.logService.log('IdentityEffects Constructor');
  }
}


