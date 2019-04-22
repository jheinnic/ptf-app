import {RouterStateSnapshot} from '@angular/router';
import {RouterStateSerializer} from '@ngrx/router-store';

import {RouterStateUrl} from './core.models';


export class CustomRouterStateSerializer implements RouterStateSerializer<RouterStateUrl>
{
  public serialize(routerState: RouterStateSnapshot): RouterStateUrl {
    const {url} = routerState;
    const {queryParams, params, queryParamMap, paramMap, data} = routerState.root;
    const retVal = {url, queryParams, params, queryParamMap, paramMap, data};

    console.log(retVal);
    return retVal;
  }
}
