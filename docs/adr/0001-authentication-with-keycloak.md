# 📄 ADR 001: Authentication via Keycloak

## Status

**Accepted** (2025-09-14)

## Context

The site (`mkopp.dev`) requires authentication for:

* Admin dashboard access (managing blog posts, portfolio entries).
* Role-based access control (e.g., `admin`, `visitor`).

Authentication must be:

* Secure and standards-compliant.
* Scalable (can handle future multi-app architecture).
* Easy to integrate with Angular (frontend) and Spring Boot (backend).
* Preferably externalized (so we don’t reinvent user management).

## Decision

We will use **Keycloak** as the authentication and identity provider.

Implementation:

* Deploy Keycloak as part of the existing Docker Compose stack.
* Integrate Spring Boot backend with Keycloak using OAuth2/OpenID Connect.
* Integrate Angular frontend using the [keycloak-angular](https://www.npmjs.com/package/keycloak-angular) library.
* Configure roles (`admin`, `public`).

## Alternatives Considered

* **Custom JWT Auth in Spring Boot**

  * ✅ Simple to implement for small apps.
  * ❌ Requires us to manage users, passwords, roles, and refresh tokens manually.
  * ❌ Not enterprise-ready.

* **Auth0 / Firebase Authentication**

  * ✅ Managed solutions, less setup effort.
  * ❌ Ongoing cost for scaling.
  * ❌ Less control, vendor lock-in.

* **Keycloak**

  * ✅ Open-source, free, widely used in enterprise.
  * ✅ Supports OAuth2, OIDC, SAML.
  * ✅ Can be reused across multiple apps (frontend + backend).
  * ❌ Slightly heavier setup and learning curve.

## Consequences

* Positive:

  * Enterprise-grade authentication and identity management.
  * Scalable to multi-app ecosystem (future-ready).
  * Standards-compliant (OAuth2, OIDC).

* Negative:

  * Extra service to maintain in Docker stack.
  * More complexity compared to custom JWT auth.

## Related Documents

* [Design Document v0.2](./design.md)
