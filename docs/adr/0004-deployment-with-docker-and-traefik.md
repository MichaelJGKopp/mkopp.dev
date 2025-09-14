# 📄 ADR 004: CI/CD & Deployment Pipeline with Docker, Traefik, and VPS

## Status

**Accepted** (2025-09-14)

## Context

For `mkopp.dev`, we need a **reliable, automated deployment pipeline** to:

* Build the Angular SSR frontend and Spring Boot backend consistently.
* Deploy multiple instances of frontend and backend containers for resilience.
* Ensure secure HTTPS/TLS termination for the custom domain.
* Simplify deployment and updates (automatic rebuild on push).
* Showcase **industry-grade DevOps practices** as part of the portfolio.

Requirements:

1. Single developer → automation must reduce manual work.
2. Enterprise-grade practices → multiple replicas, load balancing, SSL.
3. Integration with Nx monorepo (frontend currently).
4. CI/CD pipeline should support both frontend and backend (future expansion).

---

## Decision

We will implement the following **CI/CD & deployment pipeline**:

1. **CI/CD Build Stage (GitHub Actions)**

   * On push to main branch:

     * Build multi-stage Docker images for frontend and backend.
     * Push Docker images to DockerHub.
   * **Nx** is currently only used to build the frontend.
   * Nx Cloud / caching is **not yet implemented**; only GitHub Actions caching is used.
   * **Linting is currently only in-editor**; future work may integrate linting into CI/CD.

2. **Deployment Stage (VPS + Docker Compose)**

   * VPS automatically pulls the latest images via SSH.
   * Docker Compose starts multiple instances:

     * 3x Angular SSR frontend containers
     * 3x Spring Boot backend containers
     * 1x PostgreSQL container
     * Traefik as reverse proxy with TLS/HTTPS.
   * **Zero-downtime deployment is not implemented yet**; currently old containers are stopped before new ones start. Future improvements may involve Kubernetes or Docker Swarm for rolling updates.

3. **Infrastructure & Security**

   * Traefik handles HTTPS termination and routing for frontend/backend.
   * VPS hardened, firewall enabled, minimal exposed ports.
   * Domain DNS configured with TLS, email forwarding set up.

---

## Alternatives Considered

| Alternative                  | Pros             | Cons                                                           | Reason Rejected                                  |
| ---------------------------- | ---------------- | -------------------------------------------------------------- | ------------------------------------------------ |
| Manual deployment (no CI/CD) | Simple           | Error-prone, slow, not demonstrative of DevOps skills          | Rejected — want automation and showcase pipeline |
| Separate repos for FE/BE     | Clear separation | Harder to coordinate, requires multiple CI/CD pipelines        | Rejected — Nx monorepo simplifies orchestration  |
| Static site only             | Very simple      | Cannot demonstrate backend, SSR, or multi-container deployment | Rejected — site purpose is fullstack showcase    |

---

## Consequences

**Positive:**

* Demonstrates enterprise-level **CI/CD and DevOps** practices.
* Fully automated build and deploy reduces human error.
* Multi-container deployment ensures **resilience**.
* Shows secure deployment (TLS, hardened VPS, Traefik).
* Future improvements (Nx caching, zero-downtime, frontend + backend linting/build orchestration) can be integrated as project scales.

**Negative / Limitations:**

* Zero-downtime deployment not yet implemented.
* Nx currently only builds the frontend; backend build integration is future work.
* Nx Cloud caching not yet used.
* Linting not part of CI/CD yet.
* VPS must remain online and maintained.
* Multi-container setup adds some overhead in local development.

---

## Related Documents

* [Design Document v0.2](../design.md) – shows deployment overview and architecture diagram.
* ADR 002 – Nx Monorepo with Feature-Based Modularity
* ADR 003 – SSR Angular
