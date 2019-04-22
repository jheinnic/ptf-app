import {NgModule} from '@angular/core';

import {SharedModule} from '../../shared/shared.module';
import {ToymodRoutingModule} from './toymod-routing.module';
import {RouteOneComponent} from './route-one/route-one.component';
import {RouteTwoComponent} from './route-two/route-two.component';
import {CspOneComponent} from './csp-one/csp-one.component';
import {CspTextCellComponent} from './csp-one/csp-text-cell.component';

@NgModule({
  imports: [
    SharedModule,
    ToymodRoutingModule
  ],
  declarations: [RouteOneComponent, RouteTwoComponent, CspOneComponent, CspTextCellComponent]
})
export class ToymodModule { }
