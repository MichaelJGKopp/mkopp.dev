# 📄 ADR 002: Nx Monorepo with Feature-Based Modularity

## Status

**Accepted** (2025-09-14)

## Context

`mkopp.dev` consists of:

* One Angular SSR frontend application.
* One Spring Boot backend application.

Requirements:

* Organize code in a scalable and maintainable way.
* Keep the project structured for potential future expansion (e.g., admin panel, shared libraries).
* Maintain consistency, enforce boundaries, and benefit from tooling where appropriate.
* Single developer working on the project → minimal overhead.

## Decision

We will use **Nx** to manage the monorepo while keeping **feature-based modularity** inside the Angular app.

* Angular app will be organized with `core/`, `shared/`, and `features/` folders for modularity.
* The backend is defined as an Nx app using `@nxrocks/nx-spring-boot`.

  * Nx orchestrates builds, tests, and linting for the backend.
  * Gradle/Maven remains the underlying build system.
* We consciously avoid splitting Angular into multiple apps for now, as a single app suffices.

This approach balances **enterprise-grade structure** with practical simplicity for a solo developer.

## Alternatives Considered

* **No Nx / custom folder structure**

  * ✅ Minimal tooling, less setup.
  * ❌ Lacks Nx features like dependency graph, generators, caching, and consistent project conventions.
  * ❌ Harder to scale if multiple apps or shared libraries are added later.

* **Nx with multiple Angular apps from the start**

  * ✅ Fully leverages Nx multi-app features.
  * ❌ Unnecessary complexity for a solo dev with only one app.

* **Nx with feature-based modularity (chosen)**

  * ✅ Scales well in the future.
  * ✅ Uses Nx tooling where helpful (lint, test, build orchestration, affected commands).
  * ✅ Fully integrates Spring Boot backend via `@nxrocks/nx-spring-boot`.
  * ✅ Simple enough for solo development today.

## Consequences

* Positive:

  * Enterprise-style repo management.
  * Backend and frontend fully integrated into Nx workspace.
  * Easy to add new apps or shared libraries in the future.
  * Standardized generators and tooling improve code quality.
* Negative:

  * Slight overhead from Nx setup and configuration.
  * Most “advanced” Nx features (affected commands, dependency graph) currently provide little benefit for a single FE + BE.

## Related Documents

* [Design Document v0.2](../design.md)
