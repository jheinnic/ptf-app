import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ChorusVtscriptComponent } from './chorus-vtscript.component';

describe('ChorusVtscriptComponent', () => {
  let component: ChorusVtscriptComponent;
  let fixture: ComponentFixture<ChorusVtscriptComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ChorusVtscriptComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChorusVtscriptComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
