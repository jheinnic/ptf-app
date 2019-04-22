import { TestBed } from '@angular/core/testing';

import { ChorusVtscriptService } from './chorus-vtscript.service';

describe('ChorusVtscriptService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ChorusVtscriptService = TestBed.get(ChorusVtscriptService);
    expect(service).toBeTruthy();
  });
});
