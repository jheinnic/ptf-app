import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from '@angular/router';
import {Injectable} from '@angular/core';
import {Store} from '@ngrx/store';
import {Observable, timer} from 'rxjs';
import {map} from 'rxjs/operators';

import {AccessFeature} from '../store';


@Injectable()
export class FakeSessionResolver implements Resolve<boolean>
{
  constructor(private readonly store: Store<AccessFeature.State>)
  {
    console.log('Constructor for FakeSessionResolver');
  }

  public resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean>
  {
    console.log('Resolve Method for FakeSessionResolver');
    console.log('Resolve timer begins');
    return timer(5500).pipe(
      map(value => {
        console.log('Resolve timer fires ', value);
        return true;
      })
    );
  }
}
