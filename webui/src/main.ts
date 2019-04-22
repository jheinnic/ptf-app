import 'hammerjs';
import { enableProdMode, NgModuleRef } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments';

if (environment.production) {
  enableProdMode();
}

document.addEventListener('DOMContentLoaded', () => {
  console.log('In');
  platformBrowserDynamic().bootstrapModule(AppModule)
    // .then((moduleRef: NgModuleRef<AppModule>) => {
    //
    // })
  // .catch(err => {
  //   console.error(err);
  //   throw err;
  // });
  console.log('Out');
});
