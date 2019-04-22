import {HttpLink, HttpLinkHandler} from 'apollo-angular-link-http';

export function createHttpLink(httpLink: HttpLink, endpointUrl: string): HttpLinkHandler {
  return httpLink.create({
    uri: endpointUrl,
    includeExtensions: true
  });
}
