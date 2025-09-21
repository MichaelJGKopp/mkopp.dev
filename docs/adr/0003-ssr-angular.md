# 📄 ADR 003: SSR Angular

## Status

**Accepted** (2025-09-14)

## Context

The frontend of `mkopp.dev` serves as a **portfolio and blog**.
Requirements:

* Must be **SEO-friendly** (recruiters, hiring managers search Google).
* Needs to load quickly, be responsive, and mobile-first.
* Should showcase modern, enterprise-grade Angular practices.

## Decision

We will use **Angular Universal** to implement **Server-Side Rendering (SSR)**.

* SSR improves SEO for blog posts, portfolio pages, and documentation.
* Pre-rendered pages enhance performance on first load.
* Dynamic content (e.g., diagrams, live docs) will be hydrated client-side.

## Alternatives Considered

* **Angular SPA (No SSR)**

  * ✅ Simpler to implement.
  * ❌ Poor SEO, since search engines may not fully index JS-heavy SPAs.

* **Static Site Generator (e.g., Scully, Docusaurus)**

  * ✅ Excellent SEO, very fast.
  * ❌ Less flexibility for dynamic components (auth-protected areas, live diagrams).

* **Angular Universal (SSR)**

  * ✅ Best balance of SEO + interactivity.
  * ✅ Enterprise-standard for Angular production apps.
  * ❌ Slightly more complex deployment pipeline.

## Consequences

* Positive:

  * SEO-friendly (higher discoverability).
  * Enterprise-grade Angular showcase.
  * Future-proof for adding blog, documentation, and interactive teaching tools.

* Negative:

  * Slightly more complex build & deploy pipeline.
  * Diagrams and dynamic content need hydration (rendered client-side after SSR).

## Related Documents

* [Design Document v0.2](../design.md)
* [ADR 002 – Nx Monorepo](0002-nx-monorepo.md)