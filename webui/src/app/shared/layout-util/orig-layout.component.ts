import {ChangeDetectionStrategy, Component, Inject, Input, OnInit} from '@angular/core';
import {APP_BASE_HREF} from '@angular/common';
import {KeycloakProfile} from 'keycloak-js';
import {Store} from '@ngrx/store';
import {NGXLogger} from 'ngx-logger';
import {Observable} from 'rxjs';
import {AccessFeature} from '../../core/access/store';

@Component({
  selector: 'jchptf-layout-old',
  template: `

  `,
  styles: [
    `
      :host {
        flex-direction: column;
        flex-wrap: nowrap;
        align-items: stretch;
        justify-content: stretch;
      }

      mat-sidenav-container {
        background: rgba(0, 0, 0, 0.03);
      }

      mat-sidenav-container div.main-panel {
        height: calc(100vh - 56px - 24px)
      }

      *, ::ng-deep * {
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OldLayoutComponent implements OnInit
{
  public hasValidLogin$: Observable<boolean>;

  public userProfile$: Observable<KeycloakProfile>;

  @Input() public mainLayout: 'row' | 'column' | 'row-reverse' | 'column-reverse' = 'column';

  @Input() public showsInnerHeader = false;

  @Input() public title = 'ptf';

  constructor(
    private readonly store: Store<AccessFeature.State>,
    private readonly logService: NGXLogger,
    @Inject(APP_BASE_HREF) private readonly appBaseHref: string)
  {
    // this.logService.namespace = 'LayoutComponent';
    this.logService.info('Constructor for LayoutComponent');

    // this.hasValidLogin$ = this.store.select(AccessFeature.hasValidLogin);
    // this.userProfile$ = this.store.select(AccessFeature.getUserProfile);
  }

  public ngOnInit()
  {
    this.logService.info('I am ngOnInit for LayoutComponent');

    this.hasValidLogin$ = this.store.select(AccessFeature.hasValidLogin);
    this.userProfile$ = this.store.select(AccessFeature.getUserProfile);
  }
}
