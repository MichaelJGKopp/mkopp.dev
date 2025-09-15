# Functional Requirements – mkopp.dev

This document specifies the **functional requirements** (FRs) of mkopp.dev.  
Requirements are derived from user stories, epics, and planned features.  
Each requirement has a unique ID for traceability (FR-XX).

---

## 1. User Management & Authentication

### FR-01: The system must provide authentication via Keycloak (OIDC) {#fr-01}

### FR-02: Admins must be able to log in with elevated privileges {#fr-02}

### FR-03: Authentication must protect access to the admin dashboard and content editing {#fr-03}

---

## 2. Content Management

### FR-04: Admins must be able to create, edit, and delete blog posts via a web UI {#fr-04}

### FR-05: Blog posts must support Markdown content {#fr-05}

### FR-06: Blog posts must support metadata (title, tags, publication date) {#fr-06}

### FR-07: Blog posts must have SEO-friendly slugs and URLs {#fr-07}

### FR-08: Blog posts must be displayed on the public site in reverse chronological order {#fr-08}

---

## 3. Documentation & Showcase

### FR-09: The site must display the design document and ADRs (from `docs/` folder) {#fr-09}

### FR-10: The site must integrate or link to the Kanban/issue tracker {#fr-10}

### FR-11: The site must include interactive diagrams (Mermaid/PlantUML) {#fr-11}

### FR-12: Visitors must be able to browse documentation without authentication {#fr-12}

---

## 4. Blog & Learning Resources

### FR-13: Blog posts must be searchable and filterable by tag {#fr-13}

### FR-14: Visitors must be able to access all blog posts without authentication {#fr-14}

### FR-15: Blog posts may link directly to GitHub files (e.g., ADRs, user stories) {#fr-15}

---

## 5. Portfolio Features

### FR-16: Homepage must include project overview and purpose {#fr-16}

### FR-17: Homepage must provide links to GitHub repository and Kanban board {#fr-17}

### FR-18: Recruiters must be able to quickly understand the tech stack and deployment {#fr-18}

---

## 6. Deployment & Operations

### FR-19: Deployment pipeline must build frontend and backend apps {#fr-19}

### FR-20: CI/CD must build and push Docker images to DockerHub {#fr-20}

### FR-21: VPS must auto-deploy updated containers via Docker Compose {#fr-21}

### FR-22: Admin must be able to monitor service health (basic logs/monitoring) {#fr-22}
