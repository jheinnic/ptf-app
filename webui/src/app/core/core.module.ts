import {ModuleWithProviders, NgModule} from '@angular/core';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientModule, HttpClientXsrfModule} from '@angular/common/http';
import {APP_BASE_HREF, CommonModule} from '@angular/common';
import {BrowserModule} from '@angular/platform-browser';
import {LoggerModule, NgxLoggerLevel} from 'ngx-logger';
import {StoreRouterConnectingModule, RouterStateSerializer} from '@ngrx/router-store';
import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';

import {environment} from 'environments/environment';
import {GraphQLModule} from './graphql/graphql.module';
import {CustomRouterStateSerializer} from './store';
import {AccessModule} from './access';
import {metaReducers, reducerMap} from './store/root-feature.reducer';
import {CloudinaryModule} from '@cloudinary/angular-5.x';
import * as Cloudinary from 'cloudinary-core';
import {COOKIE_SERVICE} from './core.tokens';
import {CookieService} from 'ngx-cookie-service';

const IMPORTS = [
  BrowserModule,
  BrowserAnimationsModule,
  CommonModule,
  HttpClientModule,
  HttpClientXsrfModule,
  GraphQLModule,
];

const IMPORTS_FOR_PROVIDERS = [
  // BrowserModule.withServerTransition({appId: 'serverApp'}),
  LoggerModule.forRoot({
    serverLoggingUrl: '/api/logs',
    level: NgxLoggerLevel.INFO,
    serverLogLevel: NgxLoggerLevel.OFF
  }),
  CloudinaryModule.forRoot(Cloudinary, {
    cloud_name: environment.config.cloudinaryCloudName,
    upload_preset: environment.config.cloudinaryUploadPreset
  }),
  StoreModule.forRoot(reducerMap, { metaReducers }),
  StoreRouterConnectingModule.forRoot({stateKey: 'routerReducer'}),
  EffectsModule.forRoot([]),
  AccessModule.forRoot(),
];

export const PROVIDERS = [
  /**
   * The `RouterStateSnapshot` provided by the `Router` is a large complex structure.
   * A custom RouterStateSerializer is used to parse the `RouterStateSnapshot` provided
   * by `@ngrx/router-store` to include only the desired pieces of the snapshot.
   */
  {
    provide: RouterStateSerializer,
    useClass: CustomRouterStateSerializer
  },
  {
    provide: APP_BASE_HREF,
    useValue: environment.config.appBaseUrl
  },
  CookieService
  // {
  //   provide: COOKIE_SERVICE,
  //   useClass: CookieService
  // }
];

@NgModule({
  declarations: [],
  imports: [...IMPORTS, ...IMPORTS_FOR_PROVIDERS],
  providers: PROVIDERS,
  exports: [
    ...IMPORTS,
    ...IMPORTS_FOR_PROVIDERS.map(
      (moduleWithProviders: ModuleWithProviders) =>
        moduleWithProviders.ngModule)
  ],
})
export class CoreModule
{
  // public static forRoot(): ModuleWithProviders
  // {
  //   return {
  //     ngModule: CoreModule,
  //   };
  // }
}
