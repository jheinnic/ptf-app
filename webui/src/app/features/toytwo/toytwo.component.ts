import {Component} from '@angular/core';
import {Apollo} from 'apollo-angular';

@Component({
  selector: 'jchptf-toy-two',
  template: '{{ foo }}',
  styles: [ '' ],
})
export class ToyTwoComponent {
  constructor(apollo: Apollo) {
    apollo.query(
      gql`
        
      `
    )
  }

}
