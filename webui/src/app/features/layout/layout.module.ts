import {NgModule} from '@angular/core';

import {SharedModule} from '../../shared/shared.module';
import {LayoutComponent} from './layout.component';
import {NavbarComponent} from './navbar.component';
import {NavbarTemplateDirective} from './navbar-template.directive';

const IMPORTS = [
  SharedModule,
];

const COMPONENTS = [
  LayoutComponent,
  NavbarComponent,
  NavbarTemplateDirective
];

const ENTRY_COMPONENTS = [
];


@NgModule({
  imports: IMPORTS,
  declarations: COMPONENTS,
  exports: COMPONENTS,
  entryComponents: ENTRY_COMPONENTS,
  bootstrap: []
})
export class LayoutModule
{}
