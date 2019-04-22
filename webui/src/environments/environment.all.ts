import {ModuleWithProviders, NgModuleRef, StaticProvider, Type} from '@angular/core';
import {AppModule} from 'app/app.module';
import {EnvironmentConfigurationOptions} from 'app/shared/model';

export type AppDecorator = (modRef: NgModuleRef<AppModule>) => any;

export interface EnvironmentType
{
  production: boolean;
  isDebugMode: boolean;
  decorateModuleRef: AppDecorator;
  extraImports: Array<Type<any> | ModuleWithProviders>;
  extraProviders: StaticProvider[];
  config: EnvironmentConfigurationOptions;
}

// export function bootstrapKeycloak(modRef: NgModuleRef<AppModule>)
// {
//   const store: Store<AccessFeature.State> =
//     modRef.injector.get<Store<AccessFeature.State>>(Store);
//   store.dispatch(
//     new AccessIdentityActions.BootstrapAuthenticationClient({adapterName: 'default'})
//   );
// }

