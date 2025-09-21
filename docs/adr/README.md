# Architecture Decision Records (ADR)

This folder contains all **Architecture Decision Records (ADRs)** for `mkopp.dev`.

Each ADR documents an important architectural or design decision, including:

- The context of the decision.
- Alternatives considered.
- The chosen approach.
- Consequences of the decision.

## Best Practices

- **Naming Convention:** ADRs should be named using the format `XXXX-description.md`, where `XXXX` is a four-digit number. For example, `0001-authentication-with-keycloak.md`.
- **Status:** The status of an ADR should be clearly indicated at the top of the file. It is also recommended to include the date of the last status change, for example, `Accepted on 2025-09-14`.
- **Tooling:** For larger projects, consider using a tool like [adr-tools](https://github.com/npryce/adr-tools) to manage ADRs.

## Index

- [ADR 0000 – Template](0000-template.md)
- [ADR 0001 – Authentication with Keycloak](0001-authentication-with-keycloak.md)
- [ADR 0002 – Nx Monorepo](0002-nx-monorepo.md)
- [ADR 0003 – SSR Angular](0003-ssr-angular.md)
- [ADR 0004 – Deployment with Docker and Traefik.md](0004-deployment-with-docker-and-traefik.md)
