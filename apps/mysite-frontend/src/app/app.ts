import { afterNextRender, Component, inject, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FaConfig, FaIconComponent, FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { fontAwesomeIcons } from './shared/font-awesome-icons';
import { Navbar } from "./layout/navbar";
import { Footer } from "./layout/footer";
import { Oauth2AuthService } from './auth/oauth2-auth.service';
import { ToastComponent } from './shared/toast/toast.component';

@Component({
  imports: [RouterModule, Navbar, Footer, FaIconComponent, ToastComponent],
  selector: 'mysite-root',
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  protected title = 'mysite-frontend';
  private faIconLibrary = inject(FaIconLibrary);
  private faConfig = inject(FaConfig);
  private oauth2Service = inject(Oauth2AuthService);

  constructor() {
    afterNextRender(() => {
      this.oauth2Service.initAuthentication();
    });
  }

  ngOnInit() {
    this.initFontAwesome();
  }

  private initFontAwesome() {
    this.faConfig.defaultPrefix = 'far';  // font awesome regular 1 of 3 bundles
    this.faIconLibrary.addIcons(...fontAwesomeIcons);
  }
}
