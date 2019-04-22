import {EnvironmentConfigurationOptions} from '../app/shared/model/environment-configuration-options.interface';
import {EnvironmentType} from './environment.all';
import {AppModule} from '../app/app.module';
import {NgModuleRef} from '@angular/core';

const config: EnvironmentConfigurationOptions = {
  appBaseUrl: 'http://portfolio.jchein.name',
  apiGatewayUrl: 'http://api.jchein.name',
  mineSweeperApiUrl: 'http://localhost:3000',
  keycloakConfigPath: '/assets/keycloak.json',
  keycloakServerUrl: 'http://portfolio.jchein.name/auth',
  cloudinaryCloudName: 'steve',
  cloudinaryUploadPreset: 'artwork',
  defaultOnLoginRedirectUrl: '/route-one',
  defaultOnSignupRedirectUrl: '/route-one',
  defaultOnLogoutRedirectUrl: '/route-two',
  apolloGraphQueryUrl: 'http://www.cnn.com',
  neo4jGraphQueryUrl: 'http://www.cnn.com',
  randomArtBootstrapPath: '/'
};

export const environment: EnvironmentType = {
  production: false,
  isDebugMode: true,
  config: config,
  extraImports: [],
  extraProviders: [],
  decorateModuleRef: (moduleRef: NgModuleRef<AppModule>) => {},
};

// function injectFromEnvironment(tokenName: Exclude<Keys<typeof environment>, 'production'>): InjectionToken<string> {
//   return new InjectionToken<string>(tokenName, {
//     providedIn: 'root',
//     factory: () => environment[tokenName]
//   });
// }
//
// export const APP_BASE_URL: InjectionToken<string> = injectFromEnvironment('APP_BASE_URL');
// export const MINE_SWEEPER_API_URL: InjectionToken<string> = injectFromEnvironment('MINE_SWEEPER_API_URL');
// export const KEYCLOAK_CONFIG_URL: InjectionToken<string> = injectFromEnvironment('KEYCLOAK_CONFIG_URL');
// export const KEYCLOAK_SERVER_URL: InjectionToken<string> = injectFromEnvironment('KEYCLOAK_SERVER_URL');
// export const LOGIN_RETURN_URL: InjectionToken<string> = injectFromEnvironment('LOGIN_RETURN_URL');
// export const SIGNUP_RETURN_URL: InjectionToken<string> = injectFromEnvironment('SIGNUP_RETURN_URL');
// export const LOGOUT_RETURN_URL: InjectionToken<string> = injectFromEnvironment('LOGOUT_RETURN_URL');
// export const APOLLO_GRAPHQL_ENDPOINT_URL: InjectionToken<string> = injectFromEnvironment('APOLLO_GRAPHQL_ENDPOINT_URL');
// export const NEO4J_GRAPHQL_ENDPOINT_URL: InjectionToken<string> = injectFromEnvironment('NEO4J_GRAPHQL_ENDPOINT_URL');
// export const randomArtBootstrapPath: InjectionToken<string> = injectFromEnvironment('randomArtBootstrapPath');
