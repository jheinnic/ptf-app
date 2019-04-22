import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {ReturnFromLoginComponent} from './components/return-from-login.component';

const routes: Routes = [
  {
    path: 'session',
    pathMatch: 'prefix',
    children: [
      {
        path: 'auth/:onReturnKey',
        pathMatch: 'full',
        component: ReturnFromLoginComponent
      }, {
        path: 'auth',
        pathMatch: 'full',
        component: ReturnFromLoginComponent
        // }, {
        //   path: 'logout',
        //   pathMatch: 'full',
        //   component: LoginErrorComponent
      }
    ]
  }
];


@NgModule({
  imports: [
    RouterModule.forChild(routes)
  ],
  exports: [RouterModule]
})
export class AccessRoutingModule
{
}

