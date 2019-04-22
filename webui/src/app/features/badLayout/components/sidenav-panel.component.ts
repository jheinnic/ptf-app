import {Component} from '@angular/core';

@Component({
  moduleId: './src/app/core/layout/sidenav-panel.component',
  selector: 'jchptf-sidenav-panel',
  template: `
    <div class="side-panel-container">
      <mat-nav-list></mat-nav-list>
    </div>
  `
})
export class SidenavPanelComponent
{
  constructor()
  {
  }
}

