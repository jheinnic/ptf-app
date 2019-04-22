import {ApolloLink, FetchResult, NextLink, Operation} from 'apollo-link';
import {Injectable} from '@angular/core';
import {KeycloakService} from 'keycloak-angular';
import {Observable} from 'zen-observable-ts';
import {HttpHeaders} from '@angular/common/http';

@Injectable()
export class KeycloakApolloLink extends ApolloLink
{
  constructor(private readonly keycloakService: KeycloakService)
  {
    super((operation: Operation, forward?: NextLink): Observable<FetchResult> => {
        const context = operation.getContext();

        return Observable.from(
          this.keycloakService.addTokenToHeader(
            context.headers as HttpHeaders
          )
        )
          .flatMap<FetchResult>((headers: HttpHeaders) => {
            operation.setContext(
              Object.assign({}, context, {headers})
            );

            return forward(operation);
          });
      }
    );
  }
}

    // if (!!this.getToken()) {
    //   operation.setContext({
    //     headers: {
    //       Authorization: 'bearer ' + this.getToken()
    //     }
    //   });
    // } else {
    //   operation.setContext({
    //     headers: {
    //       Authorization: 'Basic bmVvNGo6cG9ydGZvbGlv'
    //     }
    //   });
    // }
