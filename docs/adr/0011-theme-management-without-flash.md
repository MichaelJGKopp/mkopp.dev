# 📄 ADR 011: Theme Management Without Visual Flash

## Status

**Accepted** (2025-12-04)

## Context

The application requires light/dark theme support with the following requirements:

* Persist user theme preference across sessions
* Support multiple themes (currently "tageslicht" light and "halloween" dark)
* Prevent visual "flash" when page loads or reloads
* Synchronize main site theme with code block highlighting theme
* Work correctly with Server-Side Rendering (SSR)
* Provide seamless theme switching

The challenge: Traditional theme loading causes a brief flash of default theme before JavaScript executes and applies the saved preference. This creates a poor user experience, especially on reload or initial navigation.

## Decision

We will implement **early theme loading in pure JavaScript within index.html** before Angular bootstraps, combined with an Angular ThemeService for runtime management.

Implementation:

* **Early Loading**: Pure JavaScript in `<head>` reads localStorage and applies theme
* **Data Attribute**: Theme stored on `document.documentElement.dataset['theme']`
* **CSS Variables**: Themes defined with CSS custom properties in global styles
* **Theme Service**: Angular service manages theme state and switching
* **highlight.js Sync**: Theme service also switches code highlighting CSS
* **localStorage**: Persists user preference

**index.html Early Loading:**

```html
<head>
  <link id="hljs-theme" rel="stylesheet" />
  <script>
    (() => {
      try {
        const theme = localStorage.getItem('theme') || 'tageslicht';
        document.documentElement.dataset['theme'] = theme;

        const hljsLink = document.getElementById('hljs-theme');
        const darkThemes = ['halloween'];
        hljsLink.href = darkThemes.includes(theme)
          ? 'https://cdn.jsdelivr.net/npm/highlight.js@11.9.0/styles/github-dark.css'
          : 'https://cdn.jsdelivr.net/npm/highlight.js@11.9.0/styles/github.css';
      } catch (e) {
        console.error('Error applying theme from localStorage', e);
        document.documentElement.dataset['theme'] = 'tageslicht';
      }
    })();
  </script>
</head>
```

**Angular ThemeService:**

```typescript
@Injectable({ providedIn: 'root' })
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
    const newTheme = this.currentTheme() === this.DEFAULT_THEME 
      ? this.DARK_THEME 
      : this.DEFAULT_THEME;
    this.setTheme(newTheme);
    
    // Sync highlight.js theme
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
}
```

**CSS Theme Variables:**

```scss
:root[data-theme='tageslicht'] {
  --bg-primary: #ffffff;
  --text-primary: #1f2937;
  // ... other variables
}

:root[data-theme='halloween'] {
  --bg-primary: #1a1a1a;
  --text-primary: #f3f4f6;
  // ... other variables
}
```

## Alternatives Considered

### Angular-Only Theme Service

* ✅ Simple Angular service
* ✅ No inline scripts
* ❌ **Flash on reload** - Angular boots after HTML renders
* ❌ Poor user experience
* ❌ Doesn't work well with SSR

### CSS Media Query (prefers-color-scheme)

* ✅ No JavaScript required
* ✅ Respects OS preference
* ❌ No manual theme switching
* ❌ Can't persist user override
* ❌ Less control over theme selection

### Server-Side Theme Cookie

* ✅ No flash - theme applied server-side
* ✅ Works with SSR
* ❌ Requires server-side rendering logic
* ❌ Cookie overhead on every request
* ❌ More complex implementation
* ❌ Harder to debug

### Early Loading + Angular Service (Chosen)

* ✅ **No visual flash** - theme applied before render
* ✅ User preference persisted in localStorage
* ✅ Works with SSR - early script runs on client
* ✅ Full control over theme switching
* ✅ Synchronizes code highlighting theme
* ✅ Fallback to default if error
* ❌ Inline script in index.html (minor)
* ❌ Duplication between early script and Angular service

## Consequences

### Positive

* **No Flash**: Theme applied instantly before page renders
* **Persistent**: User preference saved in localStorage
* **Fast**: No network request needed for theme detection
* **Flexible**: Easy to add new themes
* **Synchronized**: Main theme and code highlighting always match
* **Robust**: Error handling with fallback to default theme
* **SSR Compatible**: Works with Angular Universal
* **Type-Safe**: Theme types enforced in Angular service

### Negative

* **Inline Script**: Small inline JavaScript in index.html (unavoidable for no-flash)
* **Code Duplication**: Theme logic exists in both early script and Angular service
* **Maintenance**: Must update both locations when adding themes

### Implementation Notes

**Current Themes:**
* `tageslicht` (Light): Default, clean design with light backgrounds
* `halloween` (Dark): Dark theme with orange/purple accents

**Theme Switching Flow:**

1. User clicks theme toggle button
2. ThemeService.toggleTheme() called
3. Signal state updated
4. Data attribute updated on document root
5. localStorage updated
6. highlight.js CSS link updated
7. CSS variables automatically apply new colors

**Early Loading Benefits:**
* Executes in <head> before body renders
* Synchronous - no async delay
* Minimal performance impact (~1-2ms)
* Try/catch protects against localStorage errors
* Fallback ensures app always has a theme

**Future Enhancements:**
* Add system preference detection as fallback
* Implement theme transition animations
* Add more theme variants (e.g., high contrast)
* Consider class-based themes as alternative to data attributes
* Add theme preview in settings

## Related Documents

* [Design Document v0.4](../design.md)
* [ADR 010 – Markdown Rendering](./0010-markdown-rendering-with-highlightjs.md)
* [Theme Service Implementation](../../apps/mysite-frontend/src/app/shared/theme/theme.service.ts)
