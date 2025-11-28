import { afterNextRender, Component, inject, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FaConfig, FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { fontAwesomeIcons } from './shared/font-awesome-icons';
import { Navbar } from './layout/navbar';
import { Footer } from './layout/footer';
import { Oauth2AuthService } from './auth/oauth2-auth.service';
import { ToastComponent } from './shared/toast/toast.component';
import { ThemeService } from './shared/theme/theme.service';
import { ApiModule } from '@mkopp/api-clients/backend';
import { apiConfigFactory } from './shared/config/api.config';
import { AiAssistantComponent } from './ai-assistant/ai-assistant.component';

@Component({
  imports: [RouterModule, Navbar, Footer, ToastComponent, AiAssistantComponent],
  selector: 'mysite-root',
  templateUrl: './app.html',
  providers: [
    {
      provide: ApiModule,
      useFactory: () => ApiModule.forRoot(apiConfigFactory),
    },
  ],
})
export class App implements OnInit {
  protected title = 'mysite-frontend';
  private faIconLibrary = inject(FaIconLibrary);
  private faConfig = inject(FaConfig);
  private oauth2Service = inject(Oauth2AuthService);
  private themeService = inject(ThemeService);

  constructor() {
    afterNextRender(() => {
      this.oauth2Service.initAuthentication();
      this.themeService.initTheme();

      // Clean up URL hash if it contains OAuth2 code parameter
      const hash = globalThis.location.hash;
      if (hash.includes('code=')) {
        history.replaceState({}, document.title, globalThis.location.pathname);
      }
    });
  }

  ngOnInit() {
    this.initFontAwesome();
  }

  private initFontAwesome() {
    this.faConfig.defaultPrefix = 'far'; // font awesome regular 1 of 3 bundles
    this.faIconLibrary.addIcons(...fontAwesomeIcons);
  }
}
