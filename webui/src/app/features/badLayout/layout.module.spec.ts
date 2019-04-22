import { LayoutModule } from './layout.module';

describe('LayoutModule', () => {
  let coreModule: LayoutModule;

  beforeEach(() => {
    coreModule = new LayoutModule();
  });

  it('should create an instance', () => {
    expect(coreModule).toBeTruthy();
  });
});
