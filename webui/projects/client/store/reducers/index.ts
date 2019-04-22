import {ActionReducerMap, createFeatureSelector, createSelector} from '@ngrx/store';

import {LayoutSubFeature as fromLayout} from './layout.reducer';
import {ToymodSubFeature as fromToymod} from './toymod.reducer';

export namespace CoreFeature {
  /**
   * We treat each reducer like a table in a database. This means our top level state interface
   * is just a map of keys to inner state types.
   */
  export interface State {
    layout: fromLayout.State;
  }

  export const featureKey = 'core';

  /**
   * Our state is composed of a map of action reducer functions.
   * These reducer functions are called with each dispatched action
   * and the current or initial state and return a new immutable state.
   */
  export const reducerMap: ActionReducerMap<State> = {
    layout: fromLayout.reducer
  };

  export const initialState: Partial<State> = {
    layout: fromLayout.initialState,
  };


  export const selectCoreFeatureState = createFeatureSelector<State>(featureKey);

  /* Sub-Feature Entry Selectors */

  export const selectLayoutState =
    createSelector(selectCoreFeatureState, (state: State) => {
      return state.layout;
    });

  /* Sub-Feature Delegated Selectors */

  export const selectNavbarTemplateStack =
    createSelector(selectLayoutState, fromLayout.getNavbarTemplateStack);

  export const selectActiveNavbarTemplate =
    createSelector(selectNavbarTemplateStack, fromLayout.getActiveNavbarTemplate);
}
