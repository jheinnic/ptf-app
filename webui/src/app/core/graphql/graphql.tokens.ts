import {InjectionToken} from '@angular/core';

import {HttpLinkHandler} from 'apollo-angular-link-http';
import {InMemoryCache} from 'apollo-cache-inmemory';
import {ApolloLink} from 'apollo-link';
import {KeycloakApolloLink} from '../access/services';

export const APOLLO_HTTP_LINK =
  new InjectionToken<HttpLinkHandler>('ApolloHttpLink for Apollo Server');

export const NEO4J_HTTP_LINK =
  new InjectionToken<HttpLinkHandler>('ApolloHttpLink for Neo4J Server');

export const KEYCLOAK_LINK =
  new InjectionToken<KeycloakApolloLink>('ApolloLink for Keycloak HTTP Auth');

export const LOCAL_STATE_LINK =
  new InjectionToken<ApolloLink>('ApolloLink for local state');

export const APOLLO_CACHE_STRATEGY =
  new InjectionToken<InMemoryCache>('ApolloInMemoryCache');
