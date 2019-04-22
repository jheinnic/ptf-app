import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthenticatedGuard} from '../../core/access/services';
import {RouteOneComponent} from './route-one/route-one.component';
import {RouteTwoComponent} from './route-two/route-two.component';
import {CspOneComponent} from './csp-one/csp-one.component';

const routes: Routes = [
  {
    path: 'route-one',
    pathMatch: 'full',
    canActivate: [AuthenticatedGuard],
    data: {
      roles: ['user']
    },
    component: RouteOneComponent
  },

  {
    path: 'route-two',
    pathMatch: 'full',
    component: RouteTwoComponent
  },

  {
    path: 'csp-one',
    pathMatch: 'full',
    component: CspOneComponent
  }
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ToymodRoutingModule {
}
