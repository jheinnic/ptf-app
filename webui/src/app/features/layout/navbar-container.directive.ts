import {ComponentFactoryResolver, Directive, OnInit, OnDestroy, ViewContainerRef} from '@angular/core';
import {CdkPortalOutlet} from '@angular/cdk/portal';

import {Subscription} from 'rxjs';
import {NGXLogger} from 'ngx-logger';

import {NavbarComponent} from './navbar.component';
import {NavbarTemplateDirective} from './navbar-template.directive';

@Directive({
  selector: '[jchptf-navbar-container], [jchptfNavbarContainer]'
})
export class NavbarContainerDirective extends CdkPortalOutlet implements OnInit, OnDestroy {
  private currentTemplate: NavbarTemplateDirective;
  private templatesSubscription: Subscription;

  constructor(componentFactoryResolver: ComponentFactoryResolver, viewContainerRef: ViewContainerRef,
              private logger: NGXLogger, private store: NavbarComponent) {
    super(componentFactoryResolver, viewContainerRef);

    this.logger.info('Navbar Container Directive constructor');
  }

  public ngOnInit() {
    super.ngOnInit();

    this.logger.info('Navbar container onInit');

    /*
    this.templatesSubscription = this.navbar.contentTemplate.subscribe(
      (nextTemplate: NavbarTemplateDirective) => {
        this.logger.info("Navbar container changes", this.navbar.contentTemplate, nextTemplate);

        // const nextTemplate = contentList.reduce(function (best, next) {
        //   return ((!best) || (best.isDefault())) ? next : best;
        // }, undefined);

        // if ((!!nextTemplate) && (this.currentTemplate !== nextTemplate)) {
          if (this.hasAttached()) {
            this.detach();
          }

          // this.currentTemplate = nextTemplate;
          if (!! nextTemplate) {
            this.attachTemplatePortal(nextTemplate);
          }
        // }
      }
    );
      */
  }

  public ngOnDestroy() {
    super.ngOnDestroy();

    this.logger.info('Navbar container destroy'); // , this.navbar.contentTemplate);

    if (this.templatesSubscription) {
      this.templatesSubscription.unsubscribe();
      this.templatesSubscription = undefined;
    }

    if (this.hasAttached()) {
      this.detach();
      this.currentTemplate = undefined;
    }
  }
}
