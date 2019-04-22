import { ToymodSubFeature as fromToymod } from './toymod.reducer';

describe('Toymod Reducer', () => {
  describe('unknown action', () => {
    it('should return the initial state', () => {
      const action = {} as any;

      const result = fromToymod.reducer(fromToymod.initialState, action);

      expect(result).toBe(fromToymod.initialState);
    });
  });
});
