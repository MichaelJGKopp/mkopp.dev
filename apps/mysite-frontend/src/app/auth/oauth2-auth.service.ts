import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { UserResponse, UserService } from '@mkopp/api-clients/backend';
import Keycloak from 'keycloak-js';
import {
  catchError,
  from,
  interval,
  of,
  retry,
  Subscription,
  switchMap,
  tap,
} from 'rxjs';
import { environment } from '../../environments/environment';
import { ToastService } from '../shared/toast/toast.service';

@Injectable({
  providedIn: 'root',
})
export class Oauth2AuthService {
  private http = inject(HttpClient);
  private toastService = inject(ToastService);
  private userService = inject(UserService);

  notConnected = 'NOT_CONNECTED';

  accessToken: string | undefined;

  private keycloak = new Keycloak({
    url: environment.keycloak.url,
    realm: environment.keycloak.realm,
    clientId: environment.keycloak.clientId,
  });

  private tokenRefreshSubscription?: Subscription;

  private MIN_TOKEN_VALIDITY_MILLISECONDS = 60000;

  private fetchUserSignal = signal<UserResponse>({ email: this.notConnected });
  connectedUser = this.fetchUserSignal.asReadonly();

  private isAuthenticatedSignal = signal(false);
  isAuthenticated = this.isAuthenticatedSignal.asReadonly();

  public initAuthentication(): void {
    // Get saved redirect URL or use current location
    const savedRedirectUrl = localStorage.getItem('redirectUrl');
    const redirectUri = savedRedirectUrl
      ? window.location.origin + savedRedirectUrl
      : window.location.href;

    from(
      this.keycloak.init({
        flow: 'standard',
        onLoad: 'check-sso',
        redirectUri: redirectUri,
        silentCheckSsoRedirectUri:
          window.location.origin + '/silent-check-sso.html',
      })
    ).subscribe({
      next: (isAuthenticated) => {
        this.isAuthenticatedSignal.set(this.keycloak.authenticated || false);

        if (isAuthenticated) {
          this.accessToken = this.keycloak.token;
          this.fetch();
          this.initUpdateTokenRefresh();

          const justLoggedIn = localStorage.getItem('justLoggedIn');
          if (justLoggedIn === 'true') {
            this.toastService.show('Successfully logged in!', 'SUCCESS');
            localStorage.removeItem('justLoggedIn');
            localStorage.removeItem('redirectUrl');
          }
        }

        // Restore scroll position after page loads (for both login and logout)
        const scrollPosition = localStorage.getItem('scrollPosition');
        if (scrollPosition) {
          localStorage.removeItem('scrollPosition');
          // Use timeout to ensure page is fully rendered
          setTimeout(() => {
            window.scrollTo({
              top: parseInt(scrollPosition, 10),
              behavior: 'auto',
            });
          }, 300);
        }
      },
      error: (err) => {
        console.error('Keycloak initialization failed:', err);
        localStorage.removeItem('justLoggedIn');
      },
    });
  }

  initUpdateTokenRefresh(): void {
    this.tokenRefreshSubscription = interval(
      this.MIN_TOKEN_VALIDITY_MILLISECONDS
    )
      .pipe(
        switchMap(() =>
          from(this.keycloak.updateToken(this.MIN_TOKEN_VALIDITY_MILLISECONDS))
        )
      )
      .subscribe({
        next: (refreshed) => {
          if (refreshed) {
            this.accessToken = this.keycloak.token;
          }
        },
        error: (err) => {
          console.error('Token refresh failed:', err);

          if (this.keycloak.isTokenExpired()) {
            this.toastService.show(
              'Your session has expired. Please login again.',
              'WARNING',
              5000
            );
            setTimeout(() => this.logout(), 2000);
          }
        },
      });
  }

  fetch(): void {
    this.userService
      .getCurrentUser()
      .pipe(
        retry(2),
        catchError((error: HttpErrorResponse) => {
          this.toastService.show('Failed to fetch user information', 'DANGER');
          console.error('[HTTP] Failed to fetch user after retries:', error);
          return of({ email: this.notConnected });
        }),
        tap((user) => this.fetchUserSignal.set(user))
      )
      .subscribe();
  }

  login(): void {
    // Save current URL and scroll position to return after login
    localStorage.setItem(
      'redirectUrl',
      window.location.pathname + window.location.search
    );
    localStorage.setItem('scrollPosition', window.scrollY.toString());
    localStorage.setItem('justLoggedIn', 'true');
    this.keycloak.login();
  }

  logout(): void {
    this.tokenRefreshSubscription?.unsubscribe();

    // Save current URL and scroll position to return after logout
    localStorage.setItem(
      'redirectUrl',
      window.location.pathname + window.location.search
    );
    localStorage.setItem('scrollPosition', window.scrollY.toString());
    localStorage.removeItem('justLoggedIn');

    this.keycloak.logout().then(() => {
      // Sync signal after logout
      this.isAuthenticatedSignal.set(false);
    });
  }

  goToProfilePage(): void {
    this.keycloak.accountManagement();
  }

  hasRole(role: string): boolean {
    return (
      this.keycloak.hasRealmRole(role) || this.keycloak.hasResourceRole(role)
    );
  }

  getRoles(): string[] {
    return [
      ...(this.keycloak.realmAccess?.roles || []),
      ...(this.keycloak.resourceAccess?.[environment.keycloak.clientId]
        ?.roles || []),
    ];
  }
}
