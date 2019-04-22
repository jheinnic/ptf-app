import {CookieService} from 'ngx-cookie-service';
import {InjectionToken} from '@angular/core';

export const COOKIE_SERVICE = new InjectionToken<CookieService>('CookieService');
