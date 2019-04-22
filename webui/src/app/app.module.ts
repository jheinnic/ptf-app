import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BrowserModule} from '@angular/platform-browser';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {CoreModule} from './core/core.module';
import {LayoutModule} from './features/badLayout/layout.module';
import {ContactsModule} from './features/contacts/contacts.module';
import { environment } from '../environments';



@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    CoreModule,
    ...environment.extraImports,
    LayoutModule,
    ContactsModule,
    // MinesweeperModule,
    AppRoutingModule,
  ],
  providers: environment.extraProviders,
  entryComponents: [],
  bootstrap: [AppComponent],
  exports: [
    BrowserModule,
    CommonModule,
    CoreModule,
    AppRoutingModule,
  ]
})
export class AppModule {}
