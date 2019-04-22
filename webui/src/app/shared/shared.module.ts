import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {PortalModule} from '@angular/cdk/portal';
import {FlexLayoutModule} from '@angular/flex-layout';
import {
  MatButtonModule, MatCardModule, MatGridListModule, MatIconModule, MatListModule, MatMenuModule, MatSidenavModule, MatTabsModule, MatToolbarModule,
  MatDialogModule,
  MatTooltipModule,
  MatProgressSpinnerModule,
  MatProgressBarModule
} from '@angular/material';
import {LayoutModule} from '@angular/cdk/layout';
import {ColorPickerModule} from 'ngx-color-picker';

import {FlexSpacerDirective, NoContentComponent} from './layout-util';
import {SiegeDashboardComponent} from './siege-dashboard/siege-dashboard.component';
import {NavBallsDirective} from './navballs.directive';

export const COMPONENTS = [
  NavBallsDirective,
  FlexSpacerDirective,
  NoContentComponent,
  SiegeDashboardComponent,
];

export const IMPORTS = [
  CommonModule,
  RouterModule,
  ReactiveFormsModule,
  FormsModule,
  LayoutModule,
  PortalModule,
  MatIconModule,
  MatListModule,
  MatCardModule,
  MatMenuModule,
  MatTabsModule,
  MatButtonModule,
  MatDialogModule,
  MatSidenavModule,
  MatToolbarModule,
  MatTooltipModule,
  MatGridListModule,
  MatProgressBarModule,
  MatProgressSpinnerModule,
  ColorPickerModule
];

export const IMPORTS_WITH_PROVIDERS = [
  FlexLayoutModule.withConfig({
    addFlexToParent: true,
    addOrientationBps: false,
    disableDefaultBps: false,
    disableVendorPrefixes: false,
    serverLoaded: false,
    useColumnBasisZero: true,
    printWithBreakpoints: []
  }),
]

@NgModule({
  imports: [...IMPORTS, ...IMPORTS_WITH_PROVIDERS],
  declarations: COMPONENTS,
  exports: [
    ...COMPONENTS,
    ...IMPORTS,
    ...IMPORTS_WITH_PROVIDERS.map(module => {
      return module.ngModule;
    }),
    FlexLayoutModule
  ],
})
export class SharedModule {}

