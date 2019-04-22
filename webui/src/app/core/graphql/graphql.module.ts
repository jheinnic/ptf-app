import {NgModule, APP_INITIALIZER} from '@angular/core';
import {ApolloModule, Apollo, APOLLO_OPTIONS} from 'apollo-angular';
import {HttpLinkModule, HttpLink } from 'apollo-angular-link-http';

import {bootstrapApolloClient} from './bootstrap-apollo-client.function';
import {createHttpLink} from './create-http-link.function';
import {createLocalStateLink} from './create-local-state-link.function';
import {createInMemoryCache} from './create-in-memory-cache.function';
import {APOLLO_GRAPHQL_ENDPOINT_URL, NEO4J_GRAPHQL_ENDPOINT_URL} from '../../shared/di';
import {APOLLO_CACHE_STRATEGY, APOLLO_HTTP_LINK, KEYCLOAK_LINK, LOCAL_STATE_LINK, NEO4J_HTTP_LINK} from './graphql.tokens';
import {KeycloakApolloLink} from '../access/services';
import {createApolloClientOptions} from './create-apollo-client-options.function';



@NgModule({
  exports: [ApolloModule, HttpLinkModule],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: bootstrapApolloClient,
      deps: [Apollo, KEYCLOAK_LINK, APOLLO_HTTP_LINK, NEO4J_HTTP_LINK, LOCAL_STATE_LINK, APOLLO_CACHE_STRATEGY],
      multi: true,
    },
    {
      provide: APOLLO_OPTIONS,
      useFactory: createApolloClientOptions,
      deps: [APOLLO_CACHE_STRATEGY, LOCAL_STATE_LINK],
    },
    {
      provide: APOLLO_CACHE_STRATEGY,
      useFactory: createInMemoryCache,
      deps: [],
    },
    {
      provide: APOLLO_HTTP_LINK,
      useFactory: createHttpLink,
      deps: [HttpLink, APOLLO_GRAPHQL_ENDPOINT_URL],
    },
    {
      provide: NEO4J_HTTP_LINK,
      useFactory: createHttpLink,
      deps: [HttpLink, NEO4J_GRAPHQL_ENDPOINT_URL],
    },
    {
      provide: LOCAL_STATE_LINK,
      useFactory: createLocalStateLink,
      deps: [APOLLO_CACHE_STRATEGY],
    },
    {
      provide: KEYCLOAK_LINK,
      useClass: KeycloakApolloLink,
    }
  ],
})
export class GraphQLModule {}
