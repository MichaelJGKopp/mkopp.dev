# Non-Functional Requirements – mkopp.dev

This document specifies the **non-functional requirements** (NFRs) of mkopp.dev.  
NFRs define quality attributes, constraints, and operational standards.  
Each requirement has a unique ID for traceability (NFR-XX).

---

## 1. Performance

### NFR-01: SSR Angular pages must load within 2 seconds on average connections {#nfr-01}

### NFR-02: Backend APIs must respond within 500 ms under normal load {#nfr-02}

### NFR-03: The system must scale to at least 100 concurrent users {#nfr-03}

---

## 2. Security

### NFR-04: All traffic must be served over HTTPS with TLS certificates {#nfr-04}

### NFR-05: Authentication and authorization must be handled by Keycloak {#nfr-05}

### NFR-06: VPS must be hardened (firewall, limited SSH, non-root containers) {#nfr-06}

### NFR-07: Sensitive data must be stored securely (env variables, not in repo) {#nfr-07}

---

## 3. Availability & Resilience

### NFR-08: System must run with 3 frontend and 3 backend instances {#nfr-08}

### NFR-09: Reverse proxy (Traefik) must provide load balancing and failover {#nfr-09}

### NFR-10: Deployment must minimize downtime (zero-downtime planned for future with Kubernetes) {#nfr-10}

---

## 4. Maintainability

### NFR-11: Code must follow consistent linting and formatting rules {#nfr-11}

### NFR-12: Documentation must be versioned and kept up to date (design.md, ADRs, user stories) {#nfr-12}

### NFR-13: CI/CD must include automated builds and checks (tests, linting, security scanning) {#nfr-13}

---

## 5. SEO & Discoverability

### NFR-14: Angular SSR must generate SEO-friendly metadata and clean URLs {#nfr-14}

### NFR-15: Blog posts must generate sitemaps and Open Graph metadata {#nfr-15}

---

## 6. Transparency & Open Source

### NFR-16: Source code must be publicly available on GitHub {#nfr-16}

### NFR-17: ADRs, design docs, and requirements must be versioned in the repo {#nfr-17}

### NFR-18: Project management (Kanban, backlog, issues) must be publicly visible {#nfr-18}
