import {Action} from '@ngrx/store';
import {NavbarTemplateDirective} from '../../../shared/navbar/navbar-template.directive';

export namespace LayoutActions {
  export enum ActionTypes {
    PushNavbarTemplate = '[Layout] Push Navbar Template',
    PopNavbarTemplate = '[Layout] Pop Navbar Template'
  }

  export class PushNavbarTemplate implements Action {
    readonly type = ActionTypes.PushNavbarTemplate;

    constructor(public readonly payload: NavbarTemplateDirective) {
      if (!this.payload) {
        throw new Error('Incoming navbar templates must not be undefined');
      }
    }
  }

  export class PopNavbarTemplate implements Action {
    readonly type = ActionTypes.PopNavbarTemplate;

    constructor(public readonly payload: NavbarTemplateDirective) {
      if (!this.payload) {
        throw new Error('Outgoing navbar templates must not be undefined');
      }
    }
  }

  export type Actions = PushNavbarTemplate | PopNavbarTemplate;
}
