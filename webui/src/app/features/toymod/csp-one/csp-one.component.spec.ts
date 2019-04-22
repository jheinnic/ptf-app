import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CspOneComponent } from './csp-one.component';

describe('RouteTwoComponent', () => {
  let component: CspOneComponent;
  let fixture: ComponentFixture<CspOneComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CspOneComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CspOneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
