import {NgModule} from '@angular/core';
import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';

import {PlayAgainOptionsComponent} from './play-again-options/play-again-options.component';
import {CreateGameFormComponent} from './create-game-form/create-game-form.component';
import {StatusPanelComponent} from './status-panel/status-panel.component';
import {GameBoardComponent} from './game-board/game-board.component';
import {MinesweeperComponent} from './minesweeper.component';

import {SharedModule} from '../../shared/shared.module';
import {MinesweeperRoutingModule} from './minesweeper-routing.module';

import {MinesweeperEffects} from './store/effects/minesweeper.effects';
import * as fromMinesweeper from './store/reducers/minesweeper.reducer';

@NgModule({
  declarations: [MinesweeperComponent, GameBoardComponent, CreateGameFormComponent, StatusPanelComponent, PlayAgainOptionsComponent],
  imports: [
    SharedModule,
    MinesweeperRoutingModule,
    StoreModule.forFeature('minesweeper', fromMinesweeper.reducer),
    EffectsModule.forFeature([MinesweeperEffects])
  ]
})
export class MinesweeperModule {}
