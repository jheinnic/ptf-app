import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateGameFormComponent } from './create-game-form.component';

describe('CreateGameFormComponent', () => {
  let component: CreateGameFormComponent;
  let fixture: ComponentFixture<CreateGameFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateGameFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateGameFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
