import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Store} from '@ngrx/store';
import {Subscription} from 'rxjs';

import {SendBeginGame, SetMineCount, SetXSize, SetYSize} from '../store/actions/minesweeper.actions';
import {selectSetupOptions, State} from '../store/reducers/minesweeper.reducer';
import {SetupOptions} from '../store/models/minesweeper.models';

@Component({
  selector: 'jchptf-create-game-form',
  templateUrl: './create-game-form.component.html',
  styleUrls: ['./create-game-form.component.css']
})
export class CreateGameFormComponent implements OnInit, OnDestroy
{
  private readonly optionsForm: FormGroup;

  private formSubscription: Subscription;

  constructor(private readonly formBuilder: FormBuilder, private readonly store: Store<State>)
  {
    this.optionsForm = formBuilder.group({
      xSize: [3],
      ySize: [3],
      mineCount: [3]
    });

    this.store.select(selectSetupOptions)
      .toPromise()
      .then(
        (value: SetupOptions) => {
          this.optionsForm.setValue(value);
        }
      );
  }

  ngOnInit()
  {
    this.formSubscription = this.optionsForm.valueChanges.subscribe(
      (value: { xSize: number, ySize: number, mineCount: number }) => {
        this.store.dispatch(
          new SetXSize(value.xSize)
        );
        this.store.dispatch(
          new SetYSize(value.ySize)
        );
        this.store.dispatch(
          new SetMineCount(value.mineCount)
        );
      }
    );
  }

  ngOnDestroy()
  {
    this.formSubscription.unsubscribe();
  }

  public beginGame()
  {
    this.store.dispatch(
      new SendBeginGame()
    );
  }
}
