import { Component, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import {Observable} from 'rxjs';
import {Chance} from 'chance';
import * as uuid from 'uuid';

import {ContactsFeature, ContactsModels, ContactsActions} from './store';

@Component({
  selector: 'jchptf-contacts',
  templateUrl: './contacts.component.html',
  styleUrls: ['./contacts.component.css']
})
export class ContactsComponent implements OnInit {
  public allContacts: Observable<ContactsModels.Contact[]>;
  private chance: Chance.Chance;

  constructor(private store: Store<ContactsFeature.State>) {
    this.chance = Chance();
  }

  public ngOnInit() {
    this.allContacts = this.store.select(
      ContactsFeature.selectAll
    );
  }

  public makeOne() {
    const randomBytes = new Uint8Array(16);
    crypto.getRandomValues(randomBytes);
    const v4Options = {
      random: [...randomBytes]
    };

    console.log(randomBytes);

    this.store.dispatch(
      new ContactsActions.AddContact({
        contact: {
          id: uuid.v4(v4Options),
          name: this.chance.name(),
          size: this.chance.d100(),
          company: this.chance.company(),
          role: this.chance.profession()
        }
      })
    );
  }

  public deleteOne(id: string) {
    this.store.dispatch(
      new ContactsActions.DeleteContact({ id })
    );
  }
}
