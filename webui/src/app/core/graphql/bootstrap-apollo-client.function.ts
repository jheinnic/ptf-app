import {environment} from '../../../environments';
import {HttpLinkHandler} from 'apollo-angular-link-http';
import { httpHeaders } from 'apollo-angular-link-headers';
import {InMemoryCache} from 'apollo-cache-inmemory';
import {Apollo} from 'apollo-angular';
import {ApolloLink, Operation} from 'apollo-link';
import {APOLLO_CLIENT_NAME, LOCAL_CLIENT_NAME, NEO4J_CLIENT_NAME, REMOTE_CLIENT_TYPE} from './graphql.constants';
import {KeycloakApolloLink} from '../access/services/keycloak-apollo-link.service';

export function bootstrapApolloClient(
  apollo: Apollo,
  keycloakLink: KeycloakApolloLink,
  apolloLink: HttpLinkHandler,
  neo4jLink: HttpLinkHandler,
  localLink: ApolloLink,
  cache: InMemoryCache)
{
  return async function() {
    apollo.createDefault({
      link: localLink.split(
        (op: Operation) => {
          return op.extensions[REMOTE_CLIENT_TYPE] === NEO4J_CLIENT_NAME;
        },
        httpHeaders()
          .concat(keycloakLink)
          .concat(neo4jLink),
        httpHeaders()
          .concat(keycloakLink)
          .concat(apolloLink)
      ),
      cache: cache,
      connectToDevTools: !environment.production
    });
  };
}
