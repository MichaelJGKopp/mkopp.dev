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
* [ADR 003 – SSR Angular](0003-ssr-angular.md)
