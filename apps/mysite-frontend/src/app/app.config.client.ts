import { ApplicationConfig, mergeApplicationConfig } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './auth/auth.interceptor';
import { authExpiredInterceptor } from './auth/auth-expired.interceptor';
import { appConfig } from './app.config';

/**
 * Client-only configuration
 * Adds HTTP interceptors that require browser context (auth, tokens, etc.)
 */
export const appConfigClient: ApplicationConfig = mergeApplicationConfig(
  appConfig,
  {
    providers: [
      provideHttpClient(withInterceptors([authInterceptor, authExpiredInterceptor])),
    ],
  }
);
