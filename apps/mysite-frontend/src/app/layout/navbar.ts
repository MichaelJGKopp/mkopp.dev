import { Component, inject, signal, HostListener } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { faBars, faMoon, faSun } from '@fortawesome/free-solid-svg-icons';
import { Oauth2AuthService } from '../auth/oauth2-auth.service';
import { ThemeService } from '../shared/theme/theme.service';
import { AiService } from '../ai-assistant/ai.service';

@Component({
  selector: 'mysite-navbar',
  imports: [RouterLink, FaIconComponent],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar {
  private oauth2Service = inject(Oauth2AuthService);
  private themeService = inject(ThemeService);
  private aiService = inject(AiService);

  isAuthenticated = this.oauth2Service.isAuthenticated;
  isDarkMode = this.themeService.isDarkMode;

  faBars = faBars;
  faSun = faSun;
  faMoon = faMoon;

  // Navbar auto-hide on scroll
  private lastScrollTop = 0;
  isNavbarVisible = signal(true);

  @HostListener('window:scroll', [])
  onWindowScroll() {
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;

    // Show navbar when scrolling up, hide when scrolling down
    if (scrollTop > this.lastScrollTop && scrollTop > 100) {
      // Scrolling down & past threshold
      this.isNavbarVisible.set(false);
    } else {
      // Scrolling up or at top
      this.isNavbarVisible.set(true);
    }

    this.lastScrollTop = scrollTop <= 0 ? 0 : scrollTop;
  }

  login() {
    this.oauth2Service.login();
  }

  logout() {
    this.oauth2Service.logout();
  }

  onThemeToggle() {
    this.themeService.toggleTheme();
  }

  toggleAIPanel() {
    this.aiService.togglePanel();
  }
}
