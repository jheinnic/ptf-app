import {NgModule} from '@angular/core';
import {StoreModule} from '@ngrx/store';

import {SharedModule} from 'app/shared/shared.module';
import {ContactsRoutingModule} from './contacts-routing.module';
import {ContactsComponent} from './contacts.component';
import {ContactsFeature} from './store';

@NgModule({
  declarations: [ContactsComponent],
  imports: [
    SharedModule,
    StoreModule.forFeature(ContactsFeature.featureKey, ContactsFeature.reducer),
    ContactsRoutingModule,
  ]
})
export class ContactsModule
{ }
