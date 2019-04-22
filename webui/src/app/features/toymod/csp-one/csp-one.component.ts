import {ChangeDetectionStrategy, Component, HostBinding, ViewEncapsulation} from '@angular/core';
import * as Chance from 'chance';

@Component({
  selector: 'toy-csp-one',
  template: `
    <ng-container>
      <h1>CSP Membrane Toy</h1>
      <ng-template caiNavbarTemplate>
        <mat-tab-group>
          <mat-tab>
            <ng-template matTabLabel>Left</ng-template>
          </mat-tab>
          <mat-tab>
            <ng-template matTabLabel>Right</ng-template>
          </mat-tab>
        </mat-tab-group>
      </ng-template>
      <mat-grid-list cols="5" gutterSize="8px" rowHeight="1:1">
        <mat-grid-tile *ngFor="let cell of cellNodes" [colspan]="colspan(cell)" [rowspan]="rowspan(cell)">
          <toy-csp-text-cell [cell]="cell"></toy-csp-text-cell>
        </mat-grid-tile>
      </mat-grid-list>
    </ng-container>
  `,
  styleUrls: ['./csp-one.component.scss'],
  encapsulation: ViewEncapsulation.Emulated,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CspOneComponent
{
  public readonly cellNodes: Array<boolean>;

  public readonly chance: Chance.Chance;

  constructor()
  {
    this.cellNodes = [true, false, true, false, true, true, true, false, false];
    this.chance = new Chance.Chance();
  }

  @HostBinding('style.flex')
  public flex = '1 1 100%';

  @HostBinding('class.toy-csp-one')
  public flexy = true;

  public colspan(cell: boolean)
  {
    if (cell) {
      return this.chance.natural({
        min: 1,
        max: 5
      });
    }

    return 1;
  }

  public rowspan(cell: boolean)
  {
    if (cell) {
      return this.chance.natural({
        min: 1,
        max: 3
      });
    }

    return this.chance.natural({
      min: 1,
      max: 2
    });
  }
}

