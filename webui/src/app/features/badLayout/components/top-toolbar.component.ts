import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {MatDialog, MatDialogRef, MatMenu} from '@angular/material';
import {Observable} from 'rxjs';
import {take} from 'rxjs/operators';
import {KeycloakProfile} from 'keycloak-js';

import {LoginModalComponent} from './login-modal.component';
import {LayoutModels} from '../store';
import {NGXLogger} from 'ngx-logger';

const NO_USER_INFO: KeycloakProfile = {
  username: 'anonymous',
  email: undefined
};

@Component({
  moduleId: './src/app/core/layout/top-toolbar.component',
  selector: 'jchptf-top-toolbar',
  templateUrl: './_top-toolbar-02.view.html',
  styleUrls: [ './_top-toolbar-02.css' ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TopToolbarComponent
{
  @Input() public brandData$: Observable<LayoutModels.Brand>;

  @Input() public navItems$: Observable<LayoutModels.MenuItems>;

  @Input() public userProfile$: Observable<KeycloakProfile>;

  @Input() public hasValidLogin$: Observable<boolean>;

  @Input() public isNavMenuShown$: Observable<boolean>;

  public appMenu: MatMenu;

  @Output() public showNavItems: EventEmitter<void> = new EventEmitter<void>();

  @Output() public hideNavItems: EventEmitter<void> = new EventEmitter<void>();

  @Output() public beginLogout: EventEmitter<void> = new EventEmitter<void>();

  @Output() public beginRegister: EventEmitter<void> = new EventEmitter<void>();

  @Output() public beginLogin: EventEmitter<void> = new EventEmitter<void>();

  @Output() public showSettings: EventEmitter<void> = new EventEmitter<void>();

  @Output() public hideSettings: EventEmitter<void> = new EventEmitter<void>();

  private loginModalRef: MatDialogRef<LoginModalComponent>;


  constructor(private readonly dialog: MatDialog, private readonly logService: NGXLogger)
  {
  }

  public onClickLogin()
  {
    this.loginModalRef = this.dialog.open(
      LoginModalComponent, {disableClose: false} );

    this.loginModalRef.afterClosed()
      .pipe(
        take(1))
      .subscribe(
        result => {
          this.logService.info('user profile result: ' + result);
          if (result) {
            // this.userProfile = result.userProfile;
          }
        }
      );
  }
}
