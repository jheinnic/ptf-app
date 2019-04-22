import {Component} from '@angular/core';
import {ActivatedRoute, Params} from '@angular/router';
import {NGXLogger} from 'ngx-logger';

@Component({
  template: `
    <h1>Login Failure</h1>
    <button mat-button routerLink='/session/login'>Login</button>
    <button mat-button routerLink='/'>Back To Root</button>
    <button mat-button routerLink='/books'>Books</button>
    <P>{{params}}</P>
  `,
  styles: [],
  providers: [NGXLogger] // Inject a LogService namespace
})
export class ErrorModalComponent
{
  public readonly params: Params;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly logService: NGXLogger)
  {
    // this.logService.namespace = 'ErrorModalComponent';
    this.params = route.snapshot.params;
  }
}
