import { ToymodModule } from './toymod.module';

describe('ToymodModule', () => {
  let toymodModule: ToymodModule;

  beforeEach(() => {
    toymodModule = new ToymodModule();
  });

  it('should create an instance', () => {
    expect(toymodModule).toBeTruthy();
  });
});
