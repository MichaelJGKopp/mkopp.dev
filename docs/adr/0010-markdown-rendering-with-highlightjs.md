# 📄 ADR 010: Markdown Rendering with ngx-markdown and highlight.js

## Status

**Accepted** (2025-12-04)

## Context

The blog system requires:

* Rich content formatting with Markdown
* Syntax highlighting for code blocks
* Theme support (light/dark) for code
* SEO-friendly rendered HTML
* Secure rendering (XSS protection)
* Support for technical documentation

Blog posts are stored as Markdown in the database and need to be rendered dynamically in the Angular frontend with proper syntax highlighting for code examples.

## Decision

We will use **ngx-markdown 20.1.0** for Markdown rendering and **highlight.js 11.11.1** for syntax highlighting.

Implementation:

* **Markdown Parsing**: ngx-markdown handles Markdown to HTML conversion
* **Syntax Highlighting**: highlight.js provides code block highlighting
* **Theme Switching**: Dynamic CSS loading for light/dark code themes
* **Integration**: Configured in Angular app.config.ts with HIGHLIGHT_OPTIONS provider
* **Early Loading**: Theme-aware CSS loaded in index.html to prevent flash
* **Theme Service**: Manages theme state and switches highlight.js stylesheets

**Configuration:**

```typescript
// app.config.ts
providers: [
  provideMarkdown(),
  {
    provide: HIGHLIGHT_OPTIONS,
    useValue: {
      fullLibraryLoader: () => import('highlight.js'),
    },
  },
]
```

**Theme Management:**

```html
<!-- index.html - No flash on reload -->
<link id="hljs-theme" rel="stylesheet" />
<script>
  const theme = localStorage.getItem('theme') || 'tageslicht';
  const hljsLink = document.getElementById('hljs-theme');
  const darkThemes = ['halloween'];
  hljsLink.href = darkThemes.includes(theme)
    ? 'https://cdn.jsdelivr.net/npm/highlight.js@11.9.0/styles/github-dark.css'
    : 'https://cdn.jsdelivr.net/npm/highlight.js@11.9.0/styles/github.css';
</script>
```

**Usage in Components:**

```typescript
import { MarkdownModule } from 'ngx-markdown';

@Component({
  imports: [MarkdownModule],
  template: '<markdown [data]="post().content"></markdown>'
})
export class PostDetailComponent { }
```

## Alternatives Considered

### marked.js (Direct Integration)

* ✅ Lightweight and fast
* ✅ Simple API
* ❌ Requires manual Angular integration
* ❌ No built-in security sanitization
* ❌ Manual syntax highlighting integration
* ❌ More boilerplate code

### Showdown.js

* ✅ Feature-rich Markdown parser
* ✅ Extensions support
* ❌ Less maintained than marked
* ❌ Requires manual Angular integration
* ❌ Heavier than marked
* ❌ No official Angular wrapper

### Custom Markdown Service with DOMSanitizer

* ✅ Maximum control
* ✅ No third-party dependencies
* ❌ Reinventing the wheel
* ❌ Security risks if sanitization not perfect
* ❌ Complex to maintain
* ❌ No community support

### ngx-markdown + highlight.js

* ✅ Official Angular-compatible library
* ✅ Built-in DOMSanitizer integration (secure by default)
* ✅ highlight.js integration out of the box
* ✅ Actively maintained
* ✅ Strong community support
* ✅ Supports Angular SSR
* ✅ Compatible with Angular 20
* ❌ Adds dependencies (ngx-markdown + highlight.js + marked)

## Consequences

### Positive

* **Security**: Built-in XSS protection via Angular DOMSanitizer
* **Rich Formatting**: Full Markdown support including tables, lists, headings
* **Syntax Highlighting**: 190+ languages supported by highlight.js
* **Theme Support**: Easy switching between light/dark code themes
* **SSR Compatible**: Works with Angular Universal for SEO
* **No Flash**: Early theme loading prevents visual flicker on page load
* **Developer Experience**: Simple component integration
* **Extensibility**: Can add custom Markdown extensions if needed

### Negative

* **Bundle Size**: Adds ~50KB (ngx-markdown) + ~80KB (highlight.js full library)
* **CDN Dependency**: Currently loads highlight.js from CDN (could be bundled)
* **Limited Customization**: Some formatting opinionated by ngx-markdown defaults

### Implementation Notes

**Current Features:**
* Markdown rendering in blog posts
* Code syntax highlighting for 190+ languages
* Light theme: GitHub style (`github.css`)
* Dark theme: GitHub Dark style (`github-dark.css`)
* Theme switching synchronized with site theme
* Early loading prevents theme flash
* Responsive typography with Tailwind prose classes

**Supported Languages (Examples):**
* TypeScript, JavaScript
* Java, Python
* SQL, Bash
* JSON, YAML, XML
* Markdown, HTML, CSS
* And 180+ more...

**Performance Optimizations:**
* highlight.js loaded via dynamic import (code splitting)
* Theme CSS loaded from CDN (cached by browser)
* Only loads highlight.js when Markdown component is used

**Future Enhancements:**
* Bundle highlight.js languages selectively to reduce size
* Add Mermaid diagram support within Markdown
* Custom Markdown extensions for callouts/admonitions
* Image lazy loading within Markdown content
* Anchor links for headings

## Related Documents

* [Design Document v0.4](../design.md)
* [ngx-markdown Documentation](https://github.com/jfcere/ngx-markdown)
* [highlight.js Documentation](https://highlightjs.org/)
* [ADR 011 – Theme Management](./0011-theme-management-without-flash.md)
