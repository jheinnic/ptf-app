import {NgModule} from '@angular/core';
import {StoreModule} from '@ngrx/store';
import {FlexLayoutModule} from '@angular/flex-layout';

import {
  AppLayoutComponent, ErrorModalComponent, LoginModalComponent,
  MaskingOverlayComponent, NavItemComponent, PageNotFoundComponent,
  SidenavPanelComponent, ToolItemComponent, TopToolbarComponent
} from './components';
import {SharedModule} from '../../shared/shared.module';
import {LayoutFeature} from './store';

const IMPORTS = [
  SharedModule,
  FlexLayoutModule,
  StoreModule.forFeature(LayoutFeature.featureKey, LayoutFeature.reducer)
];

const COMPONENTS = [
  AppLayoutComponent,
  ErrorModalComponent,
  LoginModalComponent,
  NavItemComponent,
  MaskingOverlayComponent,
  PageNotFoundComponent,
  SidenavPanelComponent,
  ToolItemComponent,
  TopToolbarComponent,
];

const ENTRY_COMPONENTS = [
  AppLayoutComponent,
  ErrorModalComponent,
  LoginModalComponent,
  NavItemComponent,
  MaskingOverlayComponent,
  SidenavPanelComponent,
  ToolItemComponent,
  TopToolbarComponent,
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
