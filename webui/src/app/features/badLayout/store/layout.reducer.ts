/// <reference lib="immutable">
import {List} from 'immutable';
import * as LayoutActions from './layout.actions';
import * as LayoutModels from './layout.models';


export const initialState: LayoutModels.State = {
  available: false,
  brand: {
    name: 'Portfolio',
    icon: 'icon.png'
  },
  showNav: false,
  navStyle: LayoutModels.NavStyle.undecided,
  navItems: List.of<LayoutModels.NavItem>(
    {
      type: 'nav',
      idKey: 'listBooks',
      hint: 'My Books',
      routerLink: '/books',
      icon: 'book',
      tooltip: {
        content: 'List my collected books',
      }
    }
  ),
  account: {
    icon: 'bell',
    toolTip: 'Account',
    ariaLabel: 'Account',
    menuItems: List.of<LayoutModels.MenuItem>(
      {
        idKey: 'menu1',
        type: 'nav',
        hint: 'View book list',
        routerLink: '/books',
        icon: 'book',
        tooltip: {
          content: 'Select to open the book list view'
        }
      }
    )
  },
  notices: {
    icon: 'bell',
    toolTip: 'Notifications',
    ariaLabel: 'Notifications',
    menuItems: List.of<LayoutModels.MenuItem>(
      {
        idKey: 'menu2',
        type: 'nav',
        hint: 'View book list',
        routerLink: '/books',
        icon: 'book',
        tooltip: {
          content: 'Select to open the book list view'
        }
      }
    )
  }
};

// function assertNever(x: never): never {
//   throw new Error('Unexpected object: ' + x);
// }

export function reducer(state = initialState, action: LayoutActions.ActionType): LayoutModels.State {
  switch (action.type) {
    case LayoutActions.APPLY_UI_BLOCK:
      return {
        ...state,
        available: false
      };

    case LayoutActions.REMOVE_UI_BLOCK:
      return {
        ...state,
        available: true
      };

    case LayoutActions.HIDE_NAV_ITEMS:
      return {
        ...state,
        showNav: false
      };

    case LayoutActions.SHOW_NAV_ITEMS:
      return {
        ...state,
        showNav: true
      };

    case LayoutActions.USE_SIDE_NAV_MODE:
      return {
        ...state,
        navStyle: LayoutModels.NavStyle.side
      };

    case LayoutActions.USE_NAV_MENU_MODE:
      return {
        ...state,
        navStyle: LayoutModels.NavStyle.menu
      };

    default:
      return state;
  }
}
