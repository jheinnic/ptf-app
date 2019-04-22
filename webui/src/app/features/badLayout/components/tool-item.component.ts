import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'jchptf-tool-item',
  templateUrl: './_tool-item.view.html',
  styleUrls: [ './_tool-item.css' ],
})
export class ToolItemComponent {
  @Input() public icon = '';
  @Input() public hint = '';
  @Input() public routerLink: string | any[] = '/';
  @Output() public readonly activate = new EventEmitter();
}
