import {NavbarTemplateDirective} from '../../navbar-template.directive';


type Undefinable<T> = T | undefined;

export namespace LayoutModels {
  export interface State {
    navbarTemplateStack: Array<NavbarTemplateDirective>;
  }
}
