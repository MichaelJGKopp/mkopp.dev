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

### Core Architecture

- [ADR 0000 – Template](0000-template.md)
- [ADR 0002 – Nx Monorepo](0002-nx-monorepo.md)
- [ADR 0003 – SSR Angular](0003-ssr-angular.md)
- [ADR 0004 – Deployment with Docker and Traefik](0004-deployment-with-docker-and-traefik.md)

### Security & Authentication

- [ADR 0001 – Authentication with Keycloak](0001-authentication-with-keycloak.md)
- [ADR 0007 – Keycloak JS over Angular-Keycloak](0007-keycloak-js-over-angular-keycloak.md)

### Data & Persistence

- [ADR 0006 – Flyway Over Liquibase](0006-flyway-over-liquibase.md)
- [ADR 0012 – SSR Blog Optimization with Database](0012-ssr-blog-optimization-with-database.md)

### AI & Machine Learning

- [ADR 0008 – Spring AI Integration](0008-spring-ai-integration.md)

### Frontend & User Experience

- [ADR 0009 – OpenAPI Code Generation](0009-openapi-code-generation.md)
- [ADR 0010 – Markdown Rendering with highlight.js](0010-markdown-rendering-with-highlightjs.md)
- [ADR 0011 – Theme Management Without Flash](0011-theme-management-without-flash.md)
