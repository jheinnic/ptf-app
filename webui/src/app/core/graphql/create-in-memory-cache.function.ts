import { InMemoryCache } from 'apollo-cache-inmemory';

export function createInMemoryCache(): InMemoryCache {
  return new InMemoryCache();
}
