import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PlayAgainOptionsComponent } from './play-again-options.component';

describe('PlayAgainOptionsComponent', () => {
  let component: PlayAgainOptionsComponent;
  let fixture: ComponentFixture<PlayAgainOptionsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PlayAgainOptionsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PlayAgainOptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
