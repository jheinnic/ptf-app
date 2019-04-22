import {Component, EventEmitter, Inject, Output} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {AccessFeature, AccessIdentityModels, AccessIdentityActions} from '../../../core/access/store';

@Component({
  moduleId: module.id,
  selector: 'jchptf-login-modal',
  templateUrl: './_login-modal.view.html',
  styleUrls: ['./_zocial.css']
})
export class LoginModalComponent
{
  // TODO: Use these output events instead of direct store access?  Are they accessible via MdDialogRef?
  @Output() public loginEvent: EventEmitter<AccessIdentityActions.RequestLoginRedirect> =
    new EventEmitter<AccessIdentityActions.RequestLoginRedirect>();
  @Output() public signupEvent: EventEmitter<AccessIdentityActions.RequestSignupRedirect> =
    new EventEmitter<AccessIdentityActions.RequestSignupRedirect>();

  constructor(
    private readonly modalRef: MatDialogRef<LoginModalComponent>,
    private readonly store: Store<AccessFeature.State>,
    @Inject(MAT_DIALOG_DATA) private readonly onReturnRedirectUrl?: string
  )
  { }

  public login(useProvider?: AccessIdentityModels.LoginProviderType) {
    const nextAction =
      new AccessIdentityActions.RequestLoginRedirect(this.onReturnRedirectUrl);

    // Now call close, returning control to the caller.
    this.store.dispatch(nextAction);
    this.modalRef.close(nextAction);
  }

  public signup() {
    const nextAction = new AccessIdentityActions.RequestSignupRedirect({
      onReturnRedirectUrl: this.onReturnRedirectUrl
    });

    this.store.dispatch(nextAction);
    this.modalRef.close(nextAction);
  }
}

