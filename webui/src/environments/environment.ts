// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
import 'zone.js/dist/zone-error';

import {EnvironmentConfigurationOptions} from '../app/shared/model';
import {EnvironmentType} from './environment.all';
import {AppModule} from '../app/app.module'; // Included with Angular CLI.
import {NgModuleRef} from '@angular/core';
import {StoreDevtoolsModule} from '@ngrx/store-devtools';

const config: EnvironmentConfigurationOptions = {
  appBaseUrl: 'http://portfolio.dev.jchein.name:4200',
  apiGatewayUrl: 'http://portfolio.dev.jchein.name:9000',
  mineSweeperApiUrl: '/game',
  keycloakConfigPath: '/assets/keycloak.json',
  keycloakServerUrl: 'http://portfolio.dev.jchein.name:28433/auth',
  cloudinaryCloudName: 'steve',
  cloudinaryUploadPreset: 'artwork',
  defaultOnLoginRedirectUrl: 'http://portfolio.dev.jchein.name:4200/session/auth',
  defaultOnSignupRedirectUrl: 'http://portfolio.dev.jchein.name:4200/session/auth',
  defaultOnLogoutRedirectUrl: 'http://portfolio.dev.jchein.name:4200/session/auth',
  apolloGraphQueryUrl: 'http://portfolio.dev.jchein.name:3000/graphql',
  neo4jGraphQueryUrl: 'http://www.cnn.com',
  randomArtBootstrapPath: '/'
};

export const environment: EnvironmentType = {
  production: false,
  isDebugMode: true,
  config: config,
  extraImports: [
    StoreDevtoolsModule.instrument()
  ],
  extraProviders: [],
  decorateModuleRef: (moduleRef: NgModuleRef<AppModule>) => {},
};
