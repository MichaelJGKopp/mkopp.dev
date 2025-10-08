import {
  HttpClient,
  HttpErrorResponse,
  HttpParams,
} from '@angular/common/http';
import {
  afterNextRender,
  inject,
  Injectable,
  signal,
} from '@angular/core';
import dayjs, { Dayjs } from 'dayjs';
import Keycloak from 'keycloak-js';
import {
  catchError,
  from,
  interval,
  Observable,
  of,
  retry,
  shareReplay,
  Subject,
  Subscription,
  switchMap,
} from 'rxjs';
import { environment } from '../../environments/environment';
import { State } from '../shared/model/state.model';
import { ConnectedUser } from '../shared/model/user.model';
import { ToastService } from '../shared/toast/toast.service';

@Injectable({
  providedIn: 'root',
})
export class Oauth2AuthService {

  private http = inject(HttpClient);
  private toastService = inject(ToastService);

  notConnected = 'NOT_CONNECTED';

  accessToken: string | undefined;

  private keycloak = new Keycloak({
    url: environment.keycloak.url,
    realm: environment.keycloak.realm,
    clientId: environment.keycloak.clientId,
  });

  private tokenRefreshSubscription?: Subscription;
  
  private MIN_TOKEN_VALIDITY_MILLISECONDS = 10000;

  private fetchUserHttp$ = new Observable<ConnectedUser>();

  private lastSeen$ = new Subject<State<Dayjs>>();
  lastSeen = this.lastSeen$.asObservable();

  constructor() {
    afterNextRender(() => {
      // this.initFetchUserCaching(false); // ToDo: add back in later
    });
  }

  private fetchUserSignal = signal(
    State.forSuccess<ConnectedUser>({ email: this.notConnected })
  );

  public initAuthentication(): void {
    from(
      this.keycloak.init({
        flow: 'standard',
        onLoad: 'check-sso',
        redirectUri: window.location.origin + '/',
        silentCheckSsoRedirectUri:
          window.location.origin + '/silent-check-sso.html',
      })
    ).subscribe({
      next: (isAuthenticated) => {
        if (isAuthenticated) {
          this.accessToken = this.keycloak.token;
          this.fetch();
          this.initUpdateTokenRefresh();
          
          const justLoggedIn = sessionStorage.getItem('justLoggedIn');
          if (justLoggedIn === 'true') {
            this.toastService.show('Successfully logged in!', 'SUCCESS');
            sessionStorage.removeItem('justLoggedIn');
          }
        }
      },
      error: (err) => {
        console.error('Keycloak initialization failed:', err);
        sessionStorage.removeItem('justLoggedIn');
      }
    });
  }

  initUpdateTokenRefresh(): void {
    this.tokenRefreshSubscription = interval(this.MIN_TOKEN_VALIDITY_MILLISECONDS)
      .pipe(
        switchMap(() =>
          from(
            this.keycloak.updateToken(this.MIN_TOKEN_VALIDITY_MILLISECONDS)
          )
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
            this.toastService.show('Your session has expired. Please login again.', 'WARNING', 5000);
            setTimeout(() => this.logout(), 2000);
          }
        },
      });
  }

  initFetchUserCaching(forceResync: boolean): void {
    const params = new HttpParams().set('forceResync', forceResync);
    this.fetchUserHttp$ = this.http
      .get<ConnectedUser>(
        `${environment.API_URL}/users/get-authenticated-user`,
        { params: params }
      )
      .pipe(
        retry(2),
        catchError((error: HttpErrorResponse) => {
          this.toastService.show('Failed to fetch user information', 'DANGER');
          console.error('[HTTP] Failed to fetch user after retries:', error);
          return of({ email: this.notConnected });
        }),
        shareReplay(1)
      );
  }

  fetch(): void {
    this.fetchUserHttp$.subscribe({
      next: (user) =>
        this.fetchUserSignal.set(State.forSuccess<ConnectedUser>(user)),
      error: (error: HttpErrorResponse) => {
        this.fetchUserSignal.set(State.forError<ConnectedUser>(error));
      },
    });
  }

  isAuthenticated(): boolean {
    return this.keycloak.authenticated || false;
  }

  login(): void {
    sessionStorage.setItem('justLoggedIn', 'true');
    this.keycloak.login();
  }

  logout(): void {
    this.tokenRefreshSubscription?.unsubscribe();
    this.keycloak.logout();
  }

  goToProfilePage(): void {
    this.keycloak.accountManagement();
  }

  handleLastSeen(userPublicId: string): void {
    const params = new HttpParams().set('publicId', userPublicId);
    this.http
      .get<Date>(`${environment.API_URL}/users/get-last-seen`, { params })
      .pipe(
        retry(2),
        catchError((error: HttpErrorResponse) => {
          this.toastService.show('Failed to fetch last seen time', 'DANGER');
          console.error('[HTTP] Failed to fetch last seen after retries:', error);
          throw error;
        })
      )
      .subscribe({
        next: (lastSeen: Date) =>
          this.lastSeen$.next(
            State.forSuccess<Dayjs>(dayjs(lastSeen))
          ),
        error: (err) =>
          this.lastSeen$.next(State.forError<Dayjs>(err)),
      });
  }
}
