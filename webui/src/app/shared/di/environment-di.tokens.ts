import {InjectionToken} from '@angular/core';
import {Keys} from 'simplytyped';
import {EnvironmentConfigurationOptions} from '../model';
import {environment} from 'environments';

// export const CHANCE = new InjectionToken<string>('CHANCE');
// export const RETRY_BACKOFF_DELAY = new InjectionToken<number>('RETRY_BACKOFF_DELAY');
// export const RETRY_COUNT = new InjectionToken<number>('RETRY_COUNT');
// const DEFAULT_KEYCLOAK_CONFIG_PATH = '/assets/keycloak.json';
// const DEFAULT_RETRY_BACKOFF_DELAY = 800;
// const DEFAULT_RETRY_COUNT = 3;

type ConfigKey = Keys<EnvironmentConfigurationOptions>;

type ConfigType<Key extends ConfigKey> = EnvironmentConfigurationOptions[Key];

function injectFromEnvironment<Key extends ConfigKey = ConfigKey> (
  tokenName: Key): InjectionToken<ConfigType<Key>>
{
  return new InjectionToken<ConfigType<Key>>(
    tokenName, {
      providedIn: 'root',
      factory: () => environment.config[tokenName]
    }
  );
}

export const APP_BASE_URL: InjectionToken<string> = injectFromEnvironment('appBaseUrl');
export const API_GATEWAY_URL: InjectionToken<string> = injectFromEnvironment('apiGatewayUrl');
export const MINE_SWEEPER_API_URL: InjectionToken<string> = injectFromEnvironment('mineSweeperApiUrl');
export const KEYCLOAK_CONFIG_URL: InjectionToken<string> = injectFromEnvironment('keycloakConfigPath');
export const KEYCLOAK_SERVER_URL: InjectionToken<string> = injectFromEnvironment('keycloakServerUrl');
export const LOGIN_RETURN_URL: InjectionToken<string> = injectFromEnvironment('defaultOnLoginRedirectUrl');
export const SIGNUP_RETURN_URL: InjectionToken<string> = injectFromEnvironment('defaultOnSignupRedirectUrl');
export const LOGOUT_RETURN_URL: InjectionToken<string> = injectFromEnvironment('defaultOnLogoutRedirectUrl');
export const APOLLO_GRAPHQL_ENDPOINT_URL: InjectionToken<string> = injectFromEnvironment('apolloGraphQueryUrl');
export const NEO4J_GRAPHQL_ENDPOINT_URL: InjectionToken<string> = injectFromEnvironment('neo4jGraphQueryUrl');

export const defaultRetriesAllowed: InjectionToken<number> = injectFromEnvironment('defaultRetriesAllowed');
export const defaultRetryBackoffMs: InjectionToken<number> = injectFromEnvironment('defaultRetryBackoffMs');
export const randomArtBootstrapPath: InjectionToken<string> = injectFromEnvironment('randomArtBootstrapPath');
