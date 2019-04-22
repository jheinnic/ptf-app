import {Directive, TemplateRef, ViewContainerRef, OnInit, OnDestroy} from '@angular/core';
import {CdkPortal} from '@angular/cdk/portal';

// import {Store} from '@ngrx/store';
import {NGXLogger} from 'ngx-logger';

// import {State} from 'app/core/store';
// import {LayoutActions} from '../../core/store';

@Directive({
  selector: '[jchptf-navbar-template], [jchptfNavbarTemplate]',
  exportAs: 'navbarTemplate'
})
export class NavbarTemplateDirective extends CdkPortal implements OnInit, OnDestroy {
  constructor(
    templateRef: TemplateRef<any>,
    viewContainerRef: ViewContainerRef,
    // private store: Store<RootStore.State>,
    private logger: NGXLogger)
  {
    super(templateRef, viewContainerRef);
    this.logger.info('NavbarTemplateDirective constructor');
  }

  public ngOnInit(): void {
    this.logger.info('Push template:', this);
    // this.store.dispatch(new LayoutActions.PushNavbarTemplate(this));
  }

  public ngOnDestroy(): void {
    this.logger.info('Pop template:', this);
    // this.store.dispatch(new LayoutActions.PopNavbarTemplate(this));
  }
}
