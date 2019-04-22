import {ChangeDetectionStrategy, Component, Inject, OnInit, Self} from '@angular/core';
import {NGXLogger} from 'ngx-logger';
import {Store} from '@ngrx/store';
import {Observable} from 'rxjs';
import {InMemoryCache} from 'apollo-cache-inmemory';
import {HttpLinkHandler} from 'apollo-angular-link-http';
import {ApolloCache} from 'apollo-cache';
import {Apollo} from 'apollo-angular';
import {KeycloakProfile} from 'keycloak-js';

import {AccessIdentityActions, AccessFeature} from 'app/core/access/store';
import {LayoutModels, LayoutFeature, LayoutActions} from '../store';
import {Router, RouterStateSnapshot, RouterState} from '@angular/router';

@Component({
  selector: 'jchptf-layout',
  templateUrl: './_app-layout.view.html',
  styleUrls: [ './_app-layout.css' ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppLayoutComponent implements OnInit
{
  /**
   * Selectors are applied with `select` operator, which returns an Observable for chosen state subtree.
   */
  public readonly brandData$: Observable<LayoutModels.Brand>;

  public readonly isSideNavShown$: Observable<boolean>;

  public readonly isNavMenuShown$: Observable<boolean>;

  public readonly navItems$: Observable<LayoutModels.MenuItems>;

  public readonly hasValidLogin$: Observable<boolean>;

  public readonly userProfile$: Observable<KeycloakProfile>;

  constructor(
    private readonly store: Store<LayoutModels.State>,
    private readonly location: RouterState,
    private readonly logService: NGXLogger,
    private readonly apollo: Apollo)
  {
    // this.logService.namespace = 'AppRootContainer';
    this.logService.info('Constructor for AppRootContainer');

    this.brandData$ = this.store.select(LayoutFeature.getBrandData);
    this.isSideNavShown$ = this.store.select(LayoutFeature.isSideNavShown);
    this.isNavMenuShown$ = this.store.select(LayoutFeature.isNavMenuShown);
    this.navItems$ = this.store.select(LayoutFeature.getNavItems);
    this.hasValidLogin$ = this.store.select(AccessFeature.hasValidLogin);
    this.userProfile$ = this.store.select(AccessFeature.getUserProfile);
  }

  public ngOnInit()
  {
    this.logService.info('I am onInit for AppRootContainer');
    this.store.dispatch(
      new LayoutActions.UseSideNavMode());
  }

  public hideNavItems()
  {
    /**
     * All state updates are handled through dispatched actions in 'container'
     * components. This provides a clear, reproducible history of state
     * updates and user interaction through the life of our
     * application.
     */
    this.store.dispatch(new LayoutActions.HideNavItems());
  }

  public showNavItems()
  {
    this.logService.debug('Showing nav items');
    this.store.dispatch(new LayoutActions.ShowNavItems());
  }

  public beginLogin()
  {
    this.hideNavItems();
    const snapshot = this.location.snapshot;
    this.store.dispatch(
      new AccessIdentityActions.RequestLoginRedirect(snapshot.url)
    );
  }

  public beginRegister()
  {
    this.hideNavItems();
    this.store.dispatch(new AccessIdentityActions.RequestSignupRedirect({
      onReturnRedirectUrl: '/'
    }));
  }

  public beginLogout()
  {
    this.hideNavItems();
    this.store.dispatch(new AccessIdentityActions.RequestLogoutRedirect({
      onReturnRedirectUrl: '/'
    }));
  }

  public hideSettings()
  {
    this.logService.debug('Hide Settings TODO');
  }

  public showSettings()
  {
    this.logService.debug('Show Settings TODO');
  }

  public onActivate(event: any)
  {
    this.logService.info('Outlet activated', event);
  }

  public onDeactivate(event: any)
  {
    this.logService.info('Outlet deactivated', event);
  }
}
