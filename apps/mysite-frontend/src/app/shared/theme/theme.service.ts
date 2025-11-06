import { computed, Injectable, signal } from '@angular/core';

export type Theme = 'tageslicht' | 'halloween';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly STORAGE_KEY = 'theme';
  private readonly DEFAULT_THEME: Theme = 'tageslicht';
  private readonly DARK_THEME: Theme = 'halloween';
  
  currentTheme = signal<Theme>(this.DEFAULT_THEME);
  isDarkMode = computed(() => this.currentTheme() === this.DARK_THEME);

  initTheme(): void {
    const savedTheme = this.getStoredTheme();
    this.setTheme(savedTheme);
  }

  toggleTheme(): void {
    const newTheme: Theme = this.currentTheme() === this.DEFAULT_THEME ? this.DARK_THEME : this.DEFAULT_THEME;
    this.setTheme(newTheme);
    
    // Toggle Highlight.js theme
    const link = document.getElementById('hljs-theme') as HTMLLinkElement;
    link.href = this.isDarkMode()
      ? 'https://cdn.jsdelivr.net/npm/highlight.js@11.9.0/styles/github-dark.css'
      : 'https://cdn.jsdelivr.net/npm/highlight.js@11.9.0/styles/github.css';
  }

  setTheme(theme: Theme): void {
    this.currentTheme.set(theme);
    document.documentElement.dataset['theme'] = theme;
    localStorage.setItem(this.STORAGE_KEY, theme);
  }

  private getStoredTheme(): Theme {
    const stored = localStorage.getItem(this.STORAGE_KEY);
    return (stored === this.DEFAULT_THEME || stored === this.DARK_THEME) ? stored : this.DEFAULT_THEME;
  }
}