import {APP_INITIALIZER, ModuleWithProviders, NgModule} from '@angular/core';
import {APP_BASE_HREF, CommonModule} from '@angular/common';
import {EffectsModule} from '@ngrx/effects';
import {StoreModule} from '@ngrx/store';
import {KeycloakAngularModule, KeycloakService} from 'keycloak-angular';

import {bootstrapKeycloakAdapter} from './bootstrap-keycloak-adapter.function';
import {AuthenticatedGuard, LoginDepartureGuard} from './services';
import {AccessFeature, AccessIdentityEffects} from './store';
import {ReturnFromLoginComponent} from './components';
import {AccessRoutingModule} from './access-routing.module';

const IMPORTS = [
  CommonModule,
  KeycloakAngularModule,
  AccessRoutingModule,
];

const IMPORTS_FOR_PROVIDERS = [
  StoreModule.forFeature(AccessFeature.featureKey, AccessFeature.reducerMap, AccessFeature.reducerOptions),
  EffectsModule.forFeature([AccessIdentityEffects]),
];

export const PROVIDERS = [
  {
    provide: APP_INITIALIZER,
    useFactory: bootstrapKeycloakAdapter,
    deps: [KeycloakService, APP_BASE_HREF],
    multi: true
  },
  AuthenticatedGuard,
  LoginDepartureGuard,
];

@NgModule({
  declarations: [ReturnFromLoginComponent],
  imports: [...IMPORTS, ...IMPORTS_FOR_PROVIDERS],
  exports: [
    ...IMPORTS,
    ...IMPORTS_FOR_PROVIDERS.map(
      (moduleWithProviders: ModuleWithProviders) =>
        moduleWithProviders.ngModule)
  ],
})
export class AccessModule
{
  public static forRoot(): ModuleWithProviders
  {
    return {
      ngModule: AccessModule,
      providers: PROVIDERS,
    };
  }
}



