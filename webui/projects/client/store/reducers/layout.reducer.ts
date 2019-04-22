import {LayoutActions} from '../actions';
import * as assert from 'assert';

import {LayoutModels} from '../models';
import {NavbarTemplateDirective} from '../../navbar-template.directive';

export namespace LayoutSubFeature {
  export type State = LayoutModels.State;

  export const initialState: State = {
    navbarTemplateStack: [] as Array<NavbarTemplateDirective>
  };

  export function reducer(state: State = initialState, action: LayoutActions.Actions): State {
    switch (action.type) {
      case LayoutActions.ActionTypes.PushNavbarTemplate: {
        assert.ok(action.payload);
        assert.ok(state.navbarTemplateStack.indexOf(action.payload) < 0);

        return {
          ...state,
          navbarTemplateStack: state.navbarTemplateStack.concat(action.payload)
        };
      }

      case LayoutActions.ActionTypes.PopNavbarTemplate: {
        assert.ok(action.payload);
        const terminalIndex = state.navbarTemplateStack.length - 1;
        assert.ok(state.navbarTemplateStack.indexOf(action.payload) === terminalIndex);

        return {
          ...state,
          navbarTemplateStack: state.navbarTemplateStack.slice(0, terminalIndex)
        };
      }

      default:
        return state;
    }
  }

  export function getNavbarTemplateStack(state: State): Array<NavbarTemplateDirective> {
    return state.navbarTemplateStack;
  }

  export function getActiveNavbarTemplate(state: Array<NavbarTemplateDirective>): NavbarTemplateDirective | undefined {
    if (state.length > 0) {
      return state[state.length - 1];
    }

    return undefined;
  }
}
