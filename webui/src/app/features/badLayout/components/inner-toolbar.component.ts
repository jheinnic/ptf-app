import {AfterViewInit, Component, HostBinding, Input} from '@angular/core';

@Component({
  moduleId: './src/app/core/layout/inner-navbar.component',
  selector: 'jchptf-inner-toolbar',
  templateUrl: './_inner-toolbar.view.html',
  styleUrls: [ './_inner-toolbar.css' ]
})
export class InnerToolbarComponent implements AfterViewInit
{
  @HostBinding('class.navbar-top') public readonly hostCss = true;

  @Input() public tabData: { routerLink: string, displayName: string }[] = [];

  public rlaSafe = false;

  constructor()
  {
    // this.rlaSafe = false;
  }

  public ngAfterViewInit()
  {
    setTimeout(() => {
      this.rlaSafe = true;
    }, 0);
  }
}

