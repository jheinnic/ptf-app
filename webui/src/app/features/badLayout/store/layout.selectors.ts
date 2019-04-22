import * as LayoutModels from './layout.models';

type State = LayoutModels.State;

export const getNavStyle = (state: State) =>
  state.navStyle;

export const isSideNavShown = (state: State) =>
  state.showNav && (state.navStyle === LayoutModels.NavStyle.side);

export const isNavMenuShown = (state: State) =>
  state.showNav && (state.navStyle === LayoutModels.NavStyle.menu);

export const isUIAvailable = (state: State) =>
  state.available;

export const getNavItems = (state: State) =>
  state.navItems;

export const getBrandData = (state: State) =>
  state.brand;
