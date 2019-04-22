import { InMemoryCache } from 'apollo-cache-inmemory';
import { ApolloLink } from 'apollo-link';

export function createApolloClientOptions(cache: InMemoryCache, link: ApolloLink)
{
  return { cache, link };
}

