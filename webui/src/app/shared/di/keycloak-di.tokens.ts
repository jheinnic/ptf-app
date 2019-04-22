import {inject, InjectionToken} from '@angular/core';

import {KeycloakOptions} from 'keycloak-angular';
import {KeycloakLoginOptions} from 'keycloak-js';

import {APP_BASE_URL} from './environment-di.tokens';
import {KEYCLOAK_SERVER_URL, LOGIN_RETURN_URL, LOGOUT_RETURN_URL, SIGNUP_RETURN_URL} from './environment-di.tokens';


export const keycloakOptions: InjectionToken<KeycloakOptions> =
  new InjectionToken<KeycloakOptions>('keycloakOptions', {
    providedIn: 'root', // forwardRef(() => CoreModule),
    factory: (): KeycloakOptions => {
      return {
        config: {
          url: inject(KEYCLOAK_SERVER_URL), // realms/Throwdown/protocol/openid-connect/auth?client_id=throwdown-webui',
          realm: 'Throwdown',
          clientId: 'throwdown-webui'
        },
        initOptions: {
          // adapter: 'default,'
          // onLoad: 'none',
          flow: 'standard',
          responseMode: 'fragment',
          checkLoginIframe: false
          // checkLoginIframeInterval: 21,
        }
      };
    }
  });

export const keycloakLoginOptions: InjectionToken<KeycloakLoginOptions> =
  new InjectionToken<KeycloakLoginOptions>('KeycloakLoginOptions', {
    providedIn: 'root',
    factory: (): KeycloakLoginOptions => {
      return {
        // locale: 'en_US',
        redirectUri: inject(APP_BASE_URL) + inject(LOGIN_RETURN_URL)
      };
    }
  });

export const keycloakRegisterOptions: InjectionToken<KeycloakLoginOptions> =
  new InjectionToken<KeycloakLoginOptions>('KeycloakRegisterOptions', {
    providedIn: 'root',
    factory: (): KeycloakLoginOptions => {
      return {
        action: 'register',
        // locale: 'en_US',
        redirectUri: inject(APP_BASE_URL) + inject(SIGNUP_RETURN_URL)
      };
    }
  });

export const keycloakLogoutOptions: InjectionToken<{ redirectUri: string }> =
  new InjectionToken<{ redirectUri: string }>('KeycloakLogoutOptions', {
    providedIn: 'root',
    factory: (): { redirectUri: string } => {
      return {
        redirectUri: inject(APP_BASE_URL) + inject(LOGOUT_RETURN_URL)
      };
    }
  });
