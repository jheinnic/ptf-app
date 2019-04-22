import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Store} from '@ngrx/store';
import {Observable} from 'rxjs';
import {NGXLogger} from 'ngx-logger';

import {LayoutFeature} from '../store';

@Component({
  selector: 'jchptf-masking-overlay',
  template: `
    <div class='overlay' *ngIf='(!isUIAvailable | async) && (!diagnosticView)'>
      <div class='loading'>
        <mat-progress-spinner mode="indeterminate" aria-label="Please wait while loading">
        </mat-progress-spinner>
      </div>
      <div class='text'>Please wait... Logging in...</div>
    </div>
  `,
  styles: [
    `
      div.loading mat-progress-spinner {
        width: 100%;
        height: 100%;
      }

      .overlay {
        position: fixed;
        display: flex;
        flex-direction: column;
        justify-content: space-around;
        align-items: center;
        z-index: 1000;
        top: 0;
        right: 0;
        width: 100vw;
        height: 100vh;
        background-color: black;
        opacity: 0.9;
      }

      .overlay div.text {
        width: 100%;
        text-align: center;
        color: white;
        font-size: 20px;
        position: fixed;
      }

      .overlay div.loading {
        width: 80vw;
        height: 80vh;
        position: fixed;
      }
    `
  ],
  providers: [NGXLogger], // TODO: Inject a LogService namespace?
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MaskingOverlayComponent implements OnInit
{
  public diagnosticView = false;

  public readonly isUIAvailable: Observable<boolean>;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly store: Store<LayoutFeature.State>,
    private readonly logService: NGXLogger)
  {
    // this.logService.namespace = 'AvailabilityOverlay';
    this.logService.info('Constructor for Availability Overlay');

    this.isUIAvailable = store.select(LayoutFeature.isUIAvailable);
  }

  public ngOnInit()
  {
    console.log('I am Availability Overlay onInit');
    const routeParams = this.route.snapshot.queryParams;

    this.diagnosticView = (
      routeParams.debug === 1
    );
  }

  public hideMe()
  {
    this.diagnosticView = true;
  }

  public showMe()
  {
    this.diagnosticView = false;
  }
}
