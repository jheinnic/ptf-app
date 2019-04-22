// const DEFAULT_LOGIN_SUCCESS_TIMEOUT = 3750;
// export const LOGIN_ERROR_RETURN_URL = new InjectionToken<string>('LOGIN_ERROR_RETURN_URL');
// export const ANONYMOUS_ERROR_RETURN_URI = new InjectionToken<string>('ANONYMOUS_ERROR_RETURN_URL');

export interface EnvironmentConfigurationOptions
{
  readonly appBaseUrl: string;

  readonly apiGatewayUrl: string;

  readonly mineSweeperApiUrl: string;

  readonly apolloGraphQueryUrl: string;

  readonly neo4jGraphQueryUrl: string;

  // readonly graphSubscribeUrl: string;

  readonly keycloakConfigPath: string; // = DEFAULT_KEYCLOAK_CONFIG_PATH;

  readonly keycloakServerUrl: string; // = DEFAULT_KEYCLOAK_CONFIG_PATH;

  readonly cloudinaryCloudName: string;

  readonly cloudinaryUploadPreset: string;

  readonly defaultOnLoginRedirectUrl: string; // = '/';

  readonly defaultOnLogoutRedirectUrl: string; // = '/';

  readonly defaultOnSignupRedirectUrl?: string; // = '/';

  readonly defaultAnonymousRoute?: string; // = '/';

  readonly defaultRetriesAllowed?: number; // = DEFAULT_RETRY_COUNT;

  readonly defaultRetryBackoffMs?: number; // = DEFAULT_RETRY_BACKOFF_MS;

  readonly randomArtBootstrapPath: string;

  // static build(director: (builder: IConfigurationOptionsBuilder) => void)
  // {
  //   const builder = new ConfigurationOptionsBuilder(new EnvironmentConfigurationOptions());
  //   director(builder);
  //   return builder.value;
  // }
}
