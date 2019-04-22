import { Component } from '@angular/core';

@Component({
  selector: 'toy-siege-dashboard',
  templateUrl: './siege-dashboard.component.html',
  styleUrls: ['./siege-dashboard.component.css']
})
export class SiegeDashboardComponent {
  cards = [
    { title: 'Card 1', cols: 2, rows: 1 },
    { title: 'Card 2', cols: 1, rows: 1 },
    { title: 'Card 3', cols: 1, rows: 2 },
    { title: 'Card 4', cols: 1, rows: 1 }
  ];
}
