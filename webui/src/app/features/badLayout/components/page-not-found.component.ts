import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Store} from '@ngrx/store';
import {Observable, of} from 'rxjs';
import {AccessFeature} from '../../../core/access/store';
import {catchError} from 'rxjs/operators';

@Component({
  selector: 'jchptf-page-not-found',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <!-- angular-flex-layout's advocated interim workaround for insufficiency of host bindings for
         attaching attribute directives to a component's host element -->
    <!-- See https://github.com/angular/flex-layout/issues/76 -->
    <div fxLayout.lt-sm="column nowrap" fxLayout.gt-xs="row wrap" fxLayoutAlign.lt-sm="flex-start center"
         fxLayoutAlign.gt-xs="space-around flex-start"
         [ngStyle]="{height: '100%', width: '100%', textAlign: 'center'}"
         [ngStyle.gt-xs]="{marginTop: '24px'}">
      <mat-card fxFlex="0 1 auto">
        <mat-card-title>404: Not Found</mat-card-title>
        <mat-card-content>
          <p>Hey! It looks like this page doesn't exist yet.</p>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" routerLink="/">Take Me Home</button>
        </mat-card-actions>
      </mat-card>
      <mat-card fxFlex="0 1 auto">
        <mat-card-title>Debugging...</mat-card-title>
        <mat-card-content>{{httpResult | async | json}}</mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="accent" (click)="newCallHttpClient();">New Call Me</button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [
    `
      @media (max-width: 599px) {
        mat-card {
          margin-top: 16px
        }
      }

      @media (min-width: 600px) {
        mat-card {
          margin-top: 24px
        }
      }
    `
  ],
})
export class PageNotFoundComponent implements OnInit
{
  public httpResult: Observable<any>;

  constructor(private readonly httpClient: HttpClient, private readonly store: Store<AccessFeature.State>)
  {
    this.httpResult = of({status: 'Not Called Yet'});
  }

  public ngOnInit()
  {
  }

  public callHttpClient()
  {
    this.doHttpCall('http://portfolio.dev.jchein.name:8000/protected/premium');
  }

  public newCallHttpClient()
  {
    this.doHttpCall('https://portfolio.dev.jchein.name:8243/apiman-gateway/JCH/echo/1.5');
  }

  private doHttpCall(url: string)
  {
    let retriesLeft = 3;
    this.httpResult = (
      this.httpClient.get(url)
        .pipe(
          catchError(
            (err, stream) => {
              if (err.error instanceof ProgressEvent) {
                return of(err);
              } else {
                console.error('Failure!', err);
                if (retriesLeft > 0) {
                  retriesLeft = retriesLeft - 1;
                  return stream;
                }

                return of({
                  status: 'Failed',
                  cause: err,
                  retries: retriesLeft
                });
              }
            }
          )
        )
    );
  }
}
