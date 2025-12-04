---
title: Implementing a No-Flash Theme System with Early JavaScript Loading
slug: no-flash-theme-system
author: Michael Kopp
publishedAt: 2025-12-04T12:00:00Z
tags:
  - themes
  - dark-mode
  - javascript
  - angular
  - ux
  - performance
description: Eliminating the flash of wrong theme on page load using early JavaScript in index.html combined with Angular's reactive ThemeService.
type: technical
status: draft
---

---

## Introduction

One of the most frustrating user experiences on modern websites is the "flash of wrong theme" — when a website briefly displays in light mode before switching to the user's preferred dark mode (or vice versa). This happens because theme preferences are typically loaded asynchronously in JavaScript after the initial HTML render.

In this post, I'll explain how I implemented a zero-flash theme system for my portfolio using early JavaScript loading in `index.html` combined with Angular's `ThemeService`.

## The Problem

Traditional theme loading follows this sequence:

```
1. Browser requests HTML
2. Server sends HTML (no theme info)
3. HTML renders (default theme shown)
4. JavaScript loads
5. JavaScript reads localStorage
6. JavaScript applies saved theme ❌ FLASH OCCURS HERE
```

**Result:** Users see a brief flash of the wrong theme (typically 100-300ms).

## The Solution

The solution is to load the theme **before** the initial HTML render using an early `<script>` tag in `index.html`:

```
1. Browser requests HTML
2. Server sends HTML (with early theme script)
3. Early script runs BEFORE render
4. Script reads localStorage
5. Script applies theme class to <html>
6. HTML renders with correct theme ✅ NO FLASH
7. Angular loads and syncs theme state
```

For the complete decision rationale, see [ADR-011: Theme Management Without Flash](../adr/0011-theme-management-without-flash.md).

## Implementation

### 1. Early Theme Loading Script

Add this `<script>` tag in the `<head>` of `index.html` **before** any CSS:

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>mkopp.dev</title>
  <base href="/">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="icon" type="image/x-icon" href="favicon.ico">
  
  <!-- CRITICAL: Load theme before CSS -->
  <script>
    (function() {
      try {
        // Read theme from localStorage (or default to 'light')
        const savedTheme = localStorage.getItem('theme') || 'light';
        
        // Apply theme class to <html> element immediately
        document.documentElement.classList.add(savedTheme);
        
        // Optional: Set data attribute for CSS selectors
        document.documentElement.setAttribute('data-theme', savedTheme);
      } catch (e) {
        // Fallback if localStorage is unavailable (e.g., private browsing)
        console.warn('Failed to load theme:', e);
        document.documentElement.classList.add('light');
      }
    })();
  </script>
  
  <!-- CSS loads AFTER theme is set -->
  <link rel="stylesheet" href="styles.css">
</head>
<body>
  <app-root></app-root>
</body>
</html>
```

**Key Points:**
- **Immediately Invoked Function Expression (IIFE):** The script runs synchronously before HTML renders
- **Blocks Rendering:** The script execution blocks HTML rendering, ensuring the theme is applied before paint
- **Error Handling:** Gracefully falls back to `light` theme if `localStorage` is unavailable
- **No Dependencies:** Pure JavaScript with no Angular dependencies

### 2. Angular ThemeService

The `ThemeService` manages theme state in Angular using signals:

```typescript
import { Injectable, signal, effect } from '@angular/core';

export type Theme = 'light' | 'dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  // Signal for reactive theme state
  private themeSignal = signal<Theme>('light');

  // Public readonly signal
  readonly theme = this.themeSignal.asReadonly();

  constructor() {
    // Initialize from current DOM state (set by early script)
    const currentTheme = this.getCurrentThemeFromDOM();
    this.themeSignal.set(currentTheme);

    // Auto-persist theme changes to localStorage
    effect(() => {
      const theme = this.themeSignal();
      this.persistTheme(theme);
    });
  }

  /**
   * Toggle between light and dark themes
   */
  toggleTheme(): void {
    const newTheme = this.themeSignal() === 'light' ? 'dark' : 'light';
    this.setTheme(newTheme);
  }

  /**
   * Set specific theme
   */
  setTheme(theme: Theme): void {
    // Update signal
    this.themeSignal.set(theme);
    
    // Update DOM immediately
    this.applyThemeToDOM(theme);
  }

  /**
   * Read current theme from DOM (set by early script)
   */
  private getCurrentThemeFromDOM(): Theme {
    const htmlElement = document.documentElement;
    
    if (htmlElement.classList.contains('dark')) {
      return 'dark';
    }
    
    if (htmlElement.classList.contains('light')) {
      return 'light';
    }
    
    // Fallback: check data attribute
    const dataTheme = htmlElement.getAttribute('data-theme');
    return (dataTheme === 'dark' || dataTheme === 'light') ? dataTheme : 'light';
  }

  /**
   * Apply theme to DOM (update CSS classes)
   */
  private applyThemeToDOM(theme: Theme): void {
    const htmlElement = document.documentElement;
    
    // Remove old theme classes
    htmlElement.classList.remove('light', 'dark');
    
    // Add new theme class
    htmlElement.classList.add(theme);
    
    // Update data attribute
    htmlElement.setAttribute('data-theme', theme);
  }

  /**
   * Persist theme to localStorage
   */
  private persistTheme(theme: Theme): void {
    try {
      localStorage.setItem('theme', theme);
    } catch (e) {
      console.warn('Failed to save theme to localStorage:', e);
    }
  }
}
```

**Key Features:**
- **Signal-Based State:** Uses Angular signals for reactive theme state
- **Auto-Persistence:** Effect automatically saves theme changes to `localStorage`
- **DOM Sync:** Reads initial theme from DOM (set by early script)
- **Type Safety:** TypeScript enforces valid theme values

### 3. Theme Toggle Component

A simple toggle button for switching themes:

```typescript
import { Component, inject } from '@angular/core';
import { ThemeService } from './theme.service';

@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  template: `
    <button 
      class="theme-toggle btn btn-ghost btn-circle"
      (click)="toggleTheme()"
      [attr.aria-label]="'Switch to ' + oppositeTheme() + ' mode'">
      @if (themeService.theme() === 'light') {
        <i class="fas fa-moon"></i>
      } @else {
        <i class="fas fa-sun"></i>
      }
    </button>
  `,
  styles: [`
    .theme-toggle {
      font-size: 1.25rem;
      transition: transform 0.2s ease;
    }
    
    .theme-toggle:hover {
      transform: scale(1.1);
    }
  `]
})
export class ThemeToggleComponent {
  themeService = inject(ThemeService);

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  oppositeTheme(): string {
    return this.themeService.theme() === 'light' ? 'dark' : 'light';
  }
}
```

### 4. CSS Theme Styles

Use CSS custom properties for theme-specific colors:

```scss
/* Light theme (default) */
:root,
html.light {
  --bg-primary: #ffffff;
  --bg-secondary: #f3f4f6;
  --text-primary: #111827;
  --text-secondary: #6b7280;
  --border-color: #e5e7eb;
  --link-color: #3b82f6;
}

/* Dark theme */
html.dark {
  --bg-primary: #1f2937;
  --bg-secondary: #111827;
  --text-primary: #f9fafb;
  --text-secondary: #9ca3af;
  --border-color: #374151;
  --link-color: #60a5fa;
}

/* Apply theme colors */
body {
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: background-color 0.3s ease, color 0.3s ease;
}

a {
  color: var(--link-color);
}

.card {
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
}
```

**Benefits:**
- **Single Source of Truth:** CSS custom properties centralize theme values
- **Smooth Transitions:** Gradual color changes instead of instant flips
- **Easy Maintenance:** Update colors in one place

### 5. DaisyUI Integration

DaisyUI provides pre-built theme support with `data-theme` attribute:

```html
<!-- index.html early script -->
<script>
  (function() {
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
  })();
</script>
```

**Tailwind Config:**

```javascript
// tailwind.config.js
module.exports = {
  daisyui: {
    themes: [
      {
        light: {
          "primary": "#3b82f6",
          "secondary": "#8b5cf6",
          "accent": "#10b981",
          "neutral": "#3d4451",
          "base-100": "#ffffff",
        },
        dark: {
          "primary": "#60a5fa",
          "secondary": "#a78bfa",
          "accent": "#34d399",
          "neutral": "#2a2e37",
          "base-100": "#1f2937",
        },
      },
    ],
  },
}
```

## Code Syntax Highlighting

The theme system also controls highlight.js theme for code blocks:

```typescript
import { Component, OnInit, inject } from '@angular/core';
import { ThemeService } from './theme.service';

@Component({
  selector: 'app-blog-post',
  template: `
    <article markdown [data]="post().content"></article>
  `
})
export class BlogPostComponent implements OnInit {
  private themeService = inject(ThemeService);

  ngOnInit(): void {
    this.loadHighlightTheme();
    
    // Watch theme changes
    effect(() => {
      const theme = this.themeService.theme();
      this.loadHighlightTheme(theme);
    });
  }

  private loadHighlightTheme(theme: 'light' | 'dark' = this.themeService.theme()): void {
    const themeFile = theme === 'dark' 
      ? 'assets/highlight-dark.css' 
      : 'assets/highlight-light.css';
    
    // Dynamically load highlight.js theme
    const linkId = 'highlight-theme';
    let link = document.getElementById(linkId) as HTMLLinkElement;
    
    if (!link) {
      link = document.createElement('link');
      link.id = linkId;
      link.rel = 'stylesheet';
      document.head.appendChild(link);
    }
    
    link.href = themeFile;
  }
}
```

For more details, see [ADR-010: Markdown Rendering with highlight.js](../adr/0010-markdown-rendering-with-highlightjs.md).

## Performance Considerations

### Render Blocking

**Trade-off:** The early script blocks HTML rendering until it completes.

**Impact:**
- Script execution time: ~1-2ms
- Alternative (async loading): ~100-300ms flash duration

**Verdict:** The 1-2ms blocking cost is negligible compared to the UX improvement.

### localStorage Performance

**Read:** ~0.1ms (synchronous)  
**Write:** ~0.1ms (synchronous)

**Risk:** LocalStorage is synchronous and can block the main thread.

**Mitigation:** Minimal usage (single key/value pair).

### CSS Custom Properties

**Performance:** Native browser support, no runtime cost.

**Browser Support:** 98%+ (IE11 excluded).

## Alternatives Considered

### 1. Angular-Only Service
**Pros:**
- No inline script
- Clean separation of concerns

**Cons:**
- Always flashes (Angular loads after initial render)
- Poor UX

### 2. CSS Media Query
```css
@media (prefers-color-scheme: dark) {
  :root {
    --bg-primary: #1f2937;
  }
}
```

**Pros:**
- No JavaScript required
- Respects OS preference

**Cons:**
- Can't override user preference
- No persistence (resets on every page load)

### 3. Server-Side Rendering with Cookies
**Pros:**
- Theme applied before HTML sent to client
- Zero flash guaranteed

**Cons:**
- Requires server-side cookie handling
- Complicates deployment (need backend session management)
- Not suitable for static hosting

## Challenges and Solutions

### Challenge 1: SSR Hydration Mismatch
**Problem:** Server renders with default theme, client hydrates with user theme.

**Solution:** Use Angular's `isPlatformBrowser` to skip theme detection on server:

```typescript
constructor(@Inject(PLATFORM_ID) private platformId: Object) {
  if (isPlatformBrowser(this.platformId)) {
    const theme = this.getCurrentThemeFromDOM();
    this.themeSignal.set(theme);
  }
}
```

### Challenge 2: LocalStorage Unavailable
**Problem:** Private browsing or localStorage quota exceeded.

**Solution:** Graceful fallback to default theme:

```javascript
try {
  const theme = localStorage.getItem('theme') || 'light';
  document.documentElement.classList.add(theme);
} catch (e) {
  document.documentElement.classList.add('light');
}
```

### Challenge 3: Multiple Tabs Sync
**Problem:** Changing theme in one tab doesn't update other tabs.

**Solution:** Listen to `storage` event:

```typescript
constructor() {
  if (isPlatformBrowser(this.platformId)) {
    window.addEventListener('storage', (e) => {
      if (e.key === 'theme' && e.newValue) {
        this.setTheme(e.newValue as Theme);
      }
    });
  }
}
```

## Testing

### Manual Testing Checklist

- [ ] Theme persists across page refreshes
- [ ] No flash on initial page load
- [ ] Theme toggle button updates instantly
- [ ] Code blocks use correct highlight.js theme
- [ ] Theme syncs across multiple tabs
- [ ] Works in private browsing mode (fallback to default)
- [ ] Works with JavaScript disabled (default theme shown)

### Automated Testing

```typescript
describe('ThemeService', () => {
  let service: ThemeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ThemeService);
    localStorage.clear();
  });

  it('should default to light theme', () => {
    expect(service.theme()).toBe('light');
  });

  it('should toggle theme', () => {
    service.toggleTheme();
    expect(service.theme()).toBe('dark');
    
    service.toggleTheme();
    expect(service.theme()).toBe('light');
  });

  it('should persist theme to localStorage', () => {
    service.setTheme('dark');
    expect(localStorage.getItem('theme')).toBe('dark');
  });

  it('should apply theme to DOM', () => {
    service.setTheme('dark');
    expect(document.documentElement.classList.contains('dark')).toBe(true);
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
  });
});
```

## Future Enhancements

1. **System Preference Detection:** Fall back to `prefers-color-scheme` if no saved theme
2. **Theme Scheduling:** Auto-switch based on time of day
3. **Custom Themes:** Allow users to create custom color schemes
4. **Accessibility:** High contrast mode for low vision users
5. **Animation:** Smooth color transitions with CSS animations

## Conclusion

The early JavaScript loading technique provides a zero-flash theme experience with minimal performance impact. By combining an inline script in `index.html` with Angular's reactive `ThemeService`, the system achieves:

- **Instant Theme Application:** No visual flash on page load
- **Persistent Preferences:** Theme saved in localStorage
- **Reactive State:** Angular signals for theme state management
- **Graceful Degradation:** Falls back to default theme if localStorage unavailable

The approach is simple, performant, and provides an excellent user experience.

## Resources

- [ADR-011: Theme Management Without Flash](../adr/0011-theme-management-without-flash.md)
- [Source Code: theme.service.ts](../../apps/mysite-frontend/src/app/shared/theme/theme.service.ts)
- [Source Code: index.html](../../apps/mysite-frontend/src/index.html)
- [DaisyUI Themes](https://daisyui.com/docs/themes/)
- [MDN: prefers-color-scheme](https://developer.mozilla.org/en-US/docs/Web/CSS/@media/prefers-color-scheme)

---

**Next Post:** Type-Safe API Clients with OpenAPI Generator and Nx
