import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContactsComponent } from './contacts.component';
import { Store, StoreModule } from '@ngrx/store';

describe('ContactComponent', () => {
  let component: ContactsComponent;
  let fixture: ComponentFixture<ContactsComponent>;
  let store: Store<any>;

  beforeEach(async() => {
    TestBed.configureTestingModule({
      imports: [ StoreModule.forRoot({}) ],
      declarations: [ ContactsComponent ]
    });

    await TestBed.compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContactsComponent);
    component = fixture.componentInstance;
    store = TestBed.get(Store);

    spyOn(store, 'dispatch').and.callThrough();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
