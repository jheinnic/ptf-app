import {KeycloakService} from 'keycloak-angular';

export function bootstrapKeycloakAdapter(
  keycloak: KeycloakService, appBaseHref: string
)
{
  return async function() {
    console.log('Registering default keycloak adapter config');
    const redirectUri = `${appBaseHref}/session/auth`;
    try {
      // Use an any object to set the init options rather than using the available
      // KeycloakInitOptions type from keycloak-angular because it excludes a key
      // for setting a default redirectUri, which we very much wish to set.
      const initOptions: any = {
        checkLoginIframe: true,
        checkLoginIframeInterval: 300,
        responseMode: 'fragment',
        flow: 'implicit',
        redirectUri,
        // 'ssl-required': 'none',
        // 'enable-cors': true,
        // 'verify-token-audience': true,
        // 'use-resource-role-mappings': true,
        // 'confidential-port': 0
      };

      await keycloak.init({
        config: {
          'realm': 'Portfolio',
          'url': 'https://portfolio.dev.jchein.name:28443/auth',
          'clientId': 'ptf-webui',
          // token?: string;
          // refreshToken?: string;
          // idToken?: string;
          // timeSkew?: number;
        },
        enableBearerInterceptor: true,
        loadUserProfileAtStartUp: true,
        bearerExcludedUrls: ['/assets', '/clients/public'],
        initOptions
      });
    } catch (error) {}
  };
}
