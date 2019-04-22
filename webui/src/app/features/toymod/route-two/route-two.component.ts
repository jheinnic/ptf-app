import { Component, OnInit, ViewEncapsulation, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'toy-route-two',
  template: `
    <h1>Route Two View</h1>

    <ng-template tdnNavbarTemplate>
      <mat-tab-group>
        <mat-tab>
          <ng-template matTabLabel>Five</ng-template>
          <ng-template matTabContent>Route two link A</ng-template>
        </mat-tab>
        <mat-tab>
          <ng-template matTabLabel>Six</ng-template>
          <ng-template matTabContent>Route two link B</ng-template>
        </mat-tab>
        <mat-tab>
          <ng-template matTabLabel>Seven</ng-template>
          <ng-template matTabContent>Route two link C</ng-template>
        </mat-tab>
      </mat-tab-group>
    </ng-template>
  `,
  styleUrls: ['./route-two.component.scss'],
  encapsulation: ViewEncapsulation.Emulated,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RouteTwoComponent {
}

