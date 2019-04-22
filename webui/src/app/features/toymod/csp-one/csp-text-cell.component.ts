import {ChangeDetectionStrategy, Component, HostBinding, Input, ViewEncapsulation} from '@angular/core';
import {Chance} from 'chance';

@Component({
  selector: 'toy-csp-text-cell',
  template: `
    <ng-container>
      <mat-grid-tile-header>
        <span>Avatar!</span>
        {{head}}
      </mat-grid-tile-header>
      {{body}}
      <mat-grid-tile-footer>{{foot}}</mat-grid-tile-footer>
    </ng-container>
  `,
  styleUrls: ['./csp-one.component.scss'],
  encapsulation: ViewEncapsulation.Emulated,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CspTextCellComponent
{
  @Input()
  public param: number;

  @Input()
  public cell: boolean;

  public head: string;
  public foot: string;
  public body: string;
  public colSpan: number;
  public rowSpan: number;

  constructor() {
    const chance = new Chance();
    this.head = chance.city();
    this.body = chance.n(function() {
      return chance.n(chance.syllable, chance.natural({min: 2, max: 7})).join('');
    }, chance.natural({min: 3, max: 5})).concat(chance.pickone(['.', '?', '!', '...'])).join(' ');
    this.foot = chance.hashtag();

    this.colSpan = 1;
    this.rowSpan = 1;
    if (chance.coin() === 'heads') {
      this.colSpan = chance.natural({min: 1, max: 4});
    } else {
      this.rowSpan = chance.natural({min: 1, max: 2});
    }
  }

  @HostBinding('style.background-color') colorCell(): string {
    return this.cell ? '#44cc11' : 'dd0022';
  }
}

