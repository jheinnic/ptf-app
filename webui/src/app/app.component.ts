import { Component } from '@angular/core';
import {NGXLogger} from 'ngx-logger';

@Component({
  selector: 'jchptf-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  public readonly title = 'Chorus Test';

  constructor( public readonly logger: NGXLogger ) {
    this.logger.debug('In AppComponent constructor');
  }
}
