import { LayoutSubFeature as fromLayout } from './layout.reducer';

describe('Layout Reducer', () => {
  describe('unknown action', () => {
    it('should return the initial state', () => {
      const action = {} as any;

      const result = fromLayout.reducer(fromLayout.initialState, action);

      expect(result).toBe(fromLayout.initialState);
    });
  });
});
