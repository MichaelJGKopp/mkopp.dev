import {
  HttpClient,
  HttpErrorResponse,
  HttpParams,
} from '@angular/common/http';
import {
  afterNextRender,
  computed,
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
  shareReplay,
  Subject,
  Subscription,
  switchMap,
} from 'rxjs';
import { environment } from '../../environments/environment';
import { State } from '../shared/model/state.model';
import { ConnectedUser } from '../shared/model/user.model';

@Injectable({
  providedIn: 'root',
})
export class Oauth2AuthService {

  http = inject(HttpClient);

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
    // Only initialize in browser after render
    afterNextRender(() => {
      this.initFetchUserCaching(false);
    });
  }

  private fetchUserSignal = signal(
    State.forSuccess<ConnectedUser>({ email: this.notConnected })
  );
  fetchUser = computed(() => this.fetchUserSignal());

  public initAuthentication(): void {
    from(
      this.keycloak.init({
        flow: 'standard',
        onLoad: 'check-sso',
        redirectUri: window.location.origin + '/',
        silentCheckSsoRedirectUri:
          window.location.origin + '/silent-check-sso.html',
      })
    ).subscribe((isAuthenticated) => {
      if (isAuthenticated) {
        this.accessToken = this.keycloak.token;
        this.fetch();
        this.initUpdateTokenRefresh();
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
            console.error('Token expired and refresh failed - logging out');
            this.logout();
          } else {
            console.warn('Temporary token refresh issue, will retry');
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
        catchError(() => of({ email: this.notConnected })),
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
      .subscribe({
        next: (lastSeen) =>
          this.lastSeen$.next(
            State.forSuccess<Dayjs>(dayjs(lastSeen))
          ),
        error: (err) =>
          this.lastSeen$.next(State.forError<Dayjs>(err)),
      });
  }
}
