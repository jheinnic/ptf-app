/// <reference path="immutable">
import {TooltipPosition} from '@angular/material';
import {EventEmitter} from '@angular/core';
import {List} from 'immutable';

//
//  AccessIdentityModels
//

export type State = CoreLayout;

export interface CoreLayout
{
  readonly available: boolean;
  readonly brand: Brand;
  readonly navItems: MenuItems;
  readonly navStyle: NavStyle;
  readonly showNav: boolean;
  readonly notices: ToolBarMenu;
  readonly account: ToolBarMenu;
}

export interface Brand {
  readonly name: string;
  readonly icon: string;
}

export enum NavStyle {
  undecided = 'undecided',
  side = 'side',
  menu = 'menu'
}

export interface ToolTip {
  content: string;
  position?: TooltipPosition; /* Default is 'after' */
}

export type ItemType = 'nav' | 'action';

export interface Item {
  readonly type: ItemType;
  readonly idKey: string;
  readonly hint?: string;
  readonly tooltip?: ToolTip;
  readonly icon?: string;
}

export interface NavItem extends Item {
  readonly type: 'nav';
  readonly routerLink: string;
}

/**
 * ActionItems are modeled to expose the contents of a 'ToolActions' object maintained inside the Toolbar
 * component.  The event names in the item instance are expected to map to EventEmitter instances from
 * that object.  The intent is that activating a given ActionItem will lead to an emit() call to the
 * EventEmitter matching the ActionItem's keyId name.
 */
export interface ActionItem extends Item {
  readonly type: 'action';
  readonly eventName: string;
}

export interface ToolActions {
  [K: string]: EventEmitter<void>;
}

export type MenuItem = NavItem | ActionItem;

// export type NavItems = List<NavItem>;

export type MenuItems = List<MenuItem>;

export interface ToolBarMenu {
  readonly icon: string;
  readonly toolTip: string;
  readonly ariaLabel: string;
  readonly menuItems: MenuItems;
}

/*
export type TabItems = List<NavItem>;

export interface ToolBarButton
{
  readonly icon: string;
  readonly toolTip: string;
}

export interface ToolBarAction extends ToolBarButton {
  readonly event: string;
}

export type NestedToolBarStyle = 'tabs' | 'graph';

export const nestedStyles: IdentityRecord<NestedToolBarStyle> = {
  tabs: 'tabs',
  graph: 'graph'
};

export interface BaseNestedToolBar {
  readonly type: NestedToolBarStyle;
  readonly nestedMenus: List<ToolBarMenu>
}

export interface NestedTabsToolBar extends BaseNestedToolBar {
  readonly type: 'tabs';
  readonly nestedTabs: TabItems;
}

export interface GraphEdge {
  readonly routerLink: string;
  readonly toolTip: string;
}

export interface NestedGraphToolBar extends BaseNestedToolBar {
  readonly type: 'graph';
  readonly up: GraphEdge;
  readonly back?: GraphEdge;
}

export type NestedToolBar = NestedTabsToolBar | NestedGraphToolBar;
*/

