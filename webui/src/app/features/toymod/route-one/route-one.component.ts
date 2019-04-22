import { Component, OnInit, ViewEncapsulation, ChangeDetectionStrategy } from '@angular/core';


@Component({
  selector: 'tdn-route-one',
  template: `
    <h1>Route One View</h1>

    <ng-template tdnNavbarTemplate>
      <mat-tab-group>
        <mat-tab label="Three">
          <h1>Some tab content</h1>
          <p>Giggity</p>
        </mat-tab>
        <mat-tab label="Four">
          <ng-template matTabContent>
            <h1>Some more tab content</h1>
            <p>Goo</p>
            <button (click)="clickMe()" mat-button>Click me!</button>
          </ng-template>
        </mat-tab>
      </mat-tab-group>
    </ng-template>
  `,
  styleUrls: ['./route-one.component.scss'],
  encapsulation: ViewEncapsulation.Emulated,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RouteOneComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

  public clickMe(): void {
    console.log('Yippy Skippy Peanut Butter', this);
  }
}

