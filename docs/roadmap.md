# Project Roadmap

This document outlines the development roadmap for mkopp.dev. The project is divided into several phases, each with a specific set of goals and features.

---

## Phase 1: Foundation & CI/CD (Completed)

**Goal:** Establish the core infrastructure, and automate the deployment process.

- [x] **Angular SSR Frontend:** Basic layout with navbar, footer, and placeholder content.
- [x] **Spring Boot Backend:** Core application setup with necessary dependencies.
- [x] **CI/CD Pipeline:** Automated build, test, and deployment pipeline using GitHub Actions.
- [x] **Docker & Traefik:** Containerize the application and set up a reverse proxy with Traefik.
- [x] **VPS Deployment:** Deploy the application to a Virtual Private Server.
- [x] **TLS & Domain:** Secure the application with TLS and configure a custom domain.

---

## Phase 2: Authentication & Authorization

**Goal:** Implement a robust authentication and authorization system.

- [x] **Keycloak Setup:** Setup Keycloak Container and configure for PCKE.
- [x] **CORS Configuration:** Implement a secure, environment-aware CORS policy.
- [ ] **Keycloak Integration:** Integrate Keycloak for user authentication and role-based access control.
- [ ] **Protected Routes:** Secure the admin dashboard and other sensitive routes.
- [ ] **User Roles:** Define user roles (e.g., admin, user) and permissions.

---

## Phase 3: Content Management

**Goal:** Build a system for managing and publishing content.

- [ ] **Admin Dashboard:** Create a dashboard for managing blog posts, projects, and other content.
- [ ] **Blog System:** Develop a fully functional blog with support for Markdown.
- [ ] **Content API:** Implement a REST API for creating, reading, updating, and deleting content.

---

## Phase 4: Documentation & Showcase

**Goal:** Enhance the project's documentation and showcase its features.

- [ ] **Publish Documentation:** Publish the design document, ADRs, and other documentation.
- [ ] **Link Kanban Board:** Integrate the project's Kanban board into the website.
- [ ] **Interactive Showcase:** Create an interactive showcase of the project's architecture and deployment pipeline.

---

## Phase 5: Expansion & Refinement

**Goal:** Expand the project's features and refine the existing codebase.

- [ ] **Blog Content:** Publish blog posts on topics such as architecture, CI/CD, and security.
- [ ] **Performance Optimization:** Optimize the application's performance and scalability.
- [ ] **Code Refactoring:** Refactor the codebase to improve its quality and maintainability.
