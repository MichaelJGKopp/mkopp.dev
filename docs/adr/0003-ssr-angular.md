# 📄 ADR 002: Nx Monorepo

## Status

**Accepted** (2025-09-14)

## Context

We need a structure to organize multiple applications and libraries:

* Angular frontend (SSR).
* Spring Boot backend.
* Shared libraries and utilities (potentially TypeScript + Java).
* Future extensions (admin panel, docs site, mobile apps).

Goals:

* Developer productivity.
* Consistent tooling across frontend & backend.
* Ability to scale project structure as the site evolves.

## Decision

We will use **Nx** to manage the project as a **monorepo**.

* Nx will manage the Angular frontend app, libraries, and utility packages.
* Backend (Spring Boot) will live inside the same repository under `/apps/backend/`, tracked by Nx as an “external project.”
* Nx executors will handle linting, testing, and builds for frontend and utilities.
* CI/CD (GitHub Actions) will use Nx cache and affected-commands to optimize build times.

## Alternatives Considered

* **Multiple Repos**

  * ✅ Simple separation of concerns.
  * ❌ Harder to coordinate frontend/backend changes.
  * ❌ More complex CI/CD setup.

* **Custom Monorepo without Nx**

  * ✅ Simpler for very small projects.
  * ❌ Lacks tooling, dependency graph, caching, and affected commands.

* **Nx Monorepo**

  * ✅ Widely adopted for Angular + TypeScript ecosystems.
  * ✅ Strong tooling (lint, test, build orchestration, dependency graph).
  * ✅ Developer productivity boost.
  * ❌ Slight learning curve.

## Consequences

* Positive:

  * Enterprise-style project structure, easily extensible.
  * Shared libraries for consistency and reusability.
  * Efficient builds in CI/CD with Nx caching.

* Negative:

  * Adds an additional layer of tooling complexity.
  * Backend (Java) integration is less native than frontend, requires custom scripts.

## Related Documents

* [Design Document v0.2](../design.md)
* [ADR 001 – Authentication with Keycloak](0001-authentication-with-keycloak.md)
* [ADR 003 – SSR Angular](0003-ssr-angular.md)

---

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
