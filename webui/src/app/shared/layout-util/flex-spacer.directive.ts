import {Directive, HostBinding} from "@angular/core";

@Directive({
  // moduleId: "app/shared/blank-area/flex-spacer.directive",
  selector: '[jchptfFlexSpacer]'
})
export class FlexSpacerDirective {
  @HostBinding('class.flex-spacer')
  public readonly hasFlex = true;
}
