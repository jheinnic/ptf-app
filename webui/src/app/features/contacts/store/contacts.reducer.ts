import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';

import { ContactActions, ContactActionTypes } from './contacts.actions';
import { Contact } from './contacts.models';

export interface State extends EntityState<Contact> {
  // additional entities state properties
}

export const adapter: EntityAdapter<Contact> = createEntityAdapter<Contact>();

export const initialState: State = adapter.getInitialState({
  // additional entity state properties
});

export function reducer(
  state = initialState,
  action: ContactActions
): State {
  switch (action.type) {
    case ContactActionTypes.AddContact: {
      return adapter.addOne(action.payload.contact, state);
    }

    case ContactActionTypes.UpsertContact: {
      return adapter.upsertOne(action.payload.contact, state);
    }

    case ContactActionTypes.AddContacts: {
      return adapter.addMany(action.payload.contacts, state);
    }

    case ContactActionTypes.UpsertContacts: {
      return adapter.upsertMany(action.payload.contacts, state);
    }

    case ContactActionTypes.UpdateContact: {
      return adapter.updateOne(action.payload.contact, state);
    }

    case ContactActionTypes.UpdateContacts: {
      return adapter.updateMany(action.payload.contacts, state);
    }

    case ContactActionTypes.DeleteContact: {
      return adapter.removeOne(action.payload.id, state);
    }

    case ContactActionTypes.DeleteContacts: {
      return adapter.removeMany(action.payload.ids, state);
    }

    case ContactActionTypes.LoadContacts: {
      return adapter.addAll(action.payload.contacts, state);
    }

    case ContactActionTypes.ClearContacts: {
      return adapter.removeAll(state);
    }

    default: {
      return state;
    }
  }
}
