import {createEntityAdapter, EntityAdapter, EntityState} from '@ngrx/entity';
import {createFeatureSelector} from '@ngrx/store';

import * as ContactsModels from './contacts.models';
import * as ContactsReducer from './contacts.reducer';
import * as ContactsActions from './contacts.actions';
import {ContactsEffects} from './contacts.effects';

export {ContactsModels, ContactsActions, ContactsEffects};

export namespace ContactsFeature
{
  export const featureKey = 'contact';

  export interface State extends EntityState<ContactsModels.Contact>
  {
    // additional entities state properties
  }

  const adapter = ContactsReducer.adapter;

  export const initialState: State = adapter.getInitialState({
    // additional entity state properties
  });

  export const reducer = ContactsReducer.reducer;

  export const selectContactFeatureState = createFeatureSelector<State>(featureKey);

  export const {
    selectIds,
    selectEntities,
    selectAll,
    selectTotal,
  } = adapter.getSelectors(selectContactFeatureState);
}
