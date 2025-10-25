import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { faBars, faMoon, faSun } from '@fortawesome/free-solid-svg-icons';
import { Oauth2AuthService } from '../auth/oauth2-auth.service';
import { ThemeService } from '../shared/theme/theme.service';
@Component({
  selector: 'mysite-navbar',
  imports: [RouterLink, FaIconComponent],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar {

  private oauth2Service = inject(Oauth2AuthService);
  private themeService = inject(ThemeService);

  isDarkMode = this.themeService.isDarkMode;

  faBars = faBars;
  faSun = faSun;
  faMoon = faMoon;

  login() {
    this.oauth2Service.login();
  }

  logout() {
    this.oauth2Service.logout();
  }

  onThemeToggle() {
    this.themeService.toggleTheme();
  }
}