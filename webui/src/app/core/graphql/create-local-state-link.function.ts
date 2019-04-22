import { withClientState } from 'apollo-link-state';
import { ApolloLink } from 'apollo-link';

export function createLocalStateLink(cache): ApolloLink {
  return withClientState({
    cache: cache,
    defaults: {
      hello: 'world'
    },
    resolvers: {
      hello: ( ) => 'world'
    },
    typeDefs: '',
    fragmentMatcher: (rootValue: any, typeCondition: string, context: any) => false
  });
}
