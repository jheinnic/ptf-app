import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Store} from '@ngrx/store';
import {NGXLogger} from 'ngx-logger';
import {AccessFeature, AccessIdentityActions, AccessIdentityModels} from '../store';

@Component({
  template: `
    <h1>Returned from login</h1>
    <h1><a routerLink="/fuggitubahdit">Destined for nowhere</a></h1>
    <P>Data:{{route.data | async | json}}
    <\P>
    <P>Fragment:{{route.data | async | json}}
    <\P>
    <P>Params:{{route.params | async | json}}
    <\P>
    <P>Query Params:{{route.queryParams | async | json}}
    <\P>
    <P>Params Map:{{route.paramMap | async | json}}
    <\P>
    <P>Query Params Map:{{route.queryParamMap | async | json}}
    <\P>
  `,
  styles: [],
  providers: [NGXLogger] // Inject a LogService namespace
})
export class ReturnFromLoginComponent implements OnInit
{
  // public authenticateSubscription: Observable<boolean>;

  constructor(
    private readonly store: Store<AccessFeature.State>,
    public readonly route: ActivatedRoute,
    private readonly logService: NGXLogger)
  {
    // this.logService.namespace = 'ReturnFromLogin';
    this.logService.info('ReturnFromLogin Constructor');
  }

  public ngOnInit()
  {
    this.logService.info('ReturnFromLogin ngOnInit()');
    const onReturnRedirectionKey = this.route.snapshot.params['onReturnKey'];
    const qp = this.route.snapshot.queryParams;

    let nextAction;
    if (qp.error || qp.error_description) {
      switch (qp.error) {
        case 'invalid_request':
        {
          nextAction = new AccessIdentityActions.ProcessLoginError({
            errorCause: AccessIdentityModels.ErrorCause.Application,
            displayMessage: qp.error_description
          });
          break;
        }
        default:
        {
          nextAction = new AccessIdentityActions.ProcessLoginError({
            errorCause: AccessIdentityModels.ErrorCause.Unknown,
            displayMessage: qp.error + ', ' + qp.error_description
          });
          break;
        }
      }
    } else {
      nextAction = new AccessIdentityActions.ReturnFromLogin({uuid: onReturnRedirectionKey});
    }

    this.store.dispatch(nextAction);
  }

  public getRoute(): string
  {
    return JSON.stringify(this.route);
  }
}
