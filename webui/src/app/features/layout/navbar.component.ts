import {Component, Input} from '@angular/core';

import {Store} from '@ngrx/store';
import {NGXLogger} from 'ngx-logger';
import {Observable} from 'rxjs';

import {NavbarTemplateDirective} from './navbar-template.directive';
import {CoreFeature} from 'app/core/store';

@Component({
  selector: 'jchptf-navbar',
  template: `
    <ng-container *ngIf="navbarTemplate as activeTemplate">
      <ng-template [cdkPortalOutlet]="activeTemplate"></ng-template>
    </ng-container>
  `,
  styleUrls: ['./_navbar.component.css']

})
export class NavbarComponent {
  // public readonly navbarTemplate: Observable<NavbarTemplateDirective>;
  @Input() public navbarTemplate: NavbarTemplateDirective;

  constructor(/*private store: Store<CoreFeature.State>,*/ private logger: NGXLogger) {
    this.logger.info('Navbar Component constructor');
    // this.navbarTemplate = this.store.select(CoreFeature.selectActiveNavbarTemplate);
  }
}
