# Test Plan: CI/CD Pipeline (TP-001)

## 1. Introduction

- **Feature/Module**: CI/CD automation for mkopp.dev
- **Author**: mkopp
- **Date**: 2025-09-15
- **Version**: v1.0

## 2. Scope

This plan verifies the automated build, test, and deployment of frontend and backend via GitHub Actions, DockerHub, and VPS redeployment.

**In Scope**

- CI pipeline execution on push
- Linting, unit, and integration tests
- Docker image build and push
- VPS auto-deployment

**Out of Scope**

- Kubernetes deployment (planned future migration)
- Manual deployments

## 3. Test Strategy

- **Levels**: Unit (services), Integration (backend APIs), E2E (deployment flow)
- **Approach**: CI executes automated jobs, verified by test artifacts and live deployment
- **Tools**: GitHub Actions, Docker, Traefik, Postgres test DB, Jest/JUnit

## 4. Test Environment

- GitHub Actions runner (Ubuntu-latest)
- VPS (Ubuntu, hardened, Docker + Traefik + Compose)
- Postgres container
- Keycloak container

## 5. Test Cases

| ID   | Description                    | Preconditions | Steps                                    | Expected Result |
|------|--------------------------------|---------------|------------------------------------------|-----------------|
| TC1  | Linting runs in CI             | Push commit   | Trigger CI                                | Linter passes   |
| TC2  | Unit tests execute             | Code present  | Trigger CI                                | Tests pass      |
| TC3  | Docker images built/pushed     | DockerHub set | Trigger CI                                | Images tagged   |
| TC4  | VPS auto-pulls images          | CI succeeds   | SSH deploy step                           | Containers updated |
| TC5  | Site responds after deploy     | CI succeeds   | Access mkopp.dev                          | HTTP 200, new version visible |

## 6. Risks & Mitigations

- VPS downtime → retry mechanism + monitoring
- DockerHub outage → mirror registry fallback (future)
- Build cache invalidation → Nx caching (future)

## 7. Schedule

- Execute every push to `main`

## 8. Approval

- Sign-off: mkopp (developer/maintainer)
