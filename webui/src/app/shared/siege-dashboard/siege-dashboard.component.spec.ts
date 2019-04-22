
import {fakeAsync, ComponentFixture, TestBed, resetFakeAsyncZone} from '@angular/core/testing';

import { SiegeDashboardComponent } from './siege-dashboard.component';

describe('SiegeDashboardComponent', () => {
  let component: SiegeDashboardComponent;
  let fixture: ComponentFixture<SiegeDashboardComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SiegeDashboardComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SiegeDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    resetFakeAsyncZone();
  }));

  it('should compile', () => {
    expect(component).toBeTruthy();
  });
});
