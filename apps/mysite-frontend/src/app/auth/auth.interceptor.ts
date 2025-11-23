import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Oauth2AuthService } from './oauth2-auth.service';
import { environment } from '../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(Oauth2AuthService);

  // Only add token to API requests
  if (req.url.includes(environment.API_URL)) {
    const token = authService.accessToken;

    if (token) {
      const clonedRequest = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      });
      return next(clonedRequest);
    }
  }

  return next(req);
};
