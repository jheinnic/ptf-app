/// <reference path="../../../../../node_modules/immutable/dist/immutable.d.ts"/>

import {createFeatureSelector, createSelector} from '@ngrx/store';
import 'immutable';

import * as LayoutModels from './layout.models';
import * as LayoutActions from './layout.actions';
import * as LayoutSelectors from './layout.selectors';
import * as LayoutReducer from './layout.reducer';
import {LayoutEffects} from './layout.effects';

export {LayoutModels, LayoutActions, LayoutEffects};

export namespace LayoutFeature
{
  export const featureKey = 'layout';

  export type State = LayoutModels.State;

  export const reducer = LayoutReducer.reducer;

  const selectFromLayoutState = createFeatureSelector<LayoutModels.State>(featureKey);

  /* Core Layout Selectors */

  export const isUIAvailable =
    createSelector(selectFromLayoutState, LayoutSelectors.isUIAvailable);

  export const getBrandData =
    createSelector(selectFromLayoutState, LayoutSelectors.getBrandData);

  export const getNavItems =
    createSelector(selectFromLayoutState, LayoutSelectors.getNavItems);

  export const getNavStyle =
    createSelector(selectFromLayoutState, LayoutSelectors.getNavStyle);

  export const isNavMenuShown =
    createSelector(selectFromLayoutState, LayoutSelectors.isNavMenuShown);

  export const isSideNavShown =
    createSelector(selectFromLayoutState, LayoutSelectors.isSideNavShown);
}

