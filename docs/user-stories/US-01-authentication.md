# User Story: Admin Login (US-001)

## Description

As an **admin**, I want to log in with Keycloak authentication so that I can access the admin dashboard.

## Acceptance Criteria

* Admin can log in via Keycloak.
* Unauthorized users cannot access the dashboard.
* Session is persisted securely.

## References

* Functional Requirement: [FR-01: Authentication via Keycloak](../requirements/functional.md#fr-01)
* Functional Requirement: [FR-02: Admin login with elevated privileges](../requirements/functional.md#fr-02)
* Non-Functional Requirement: [NFR-05: Authentication handled by Keycloak](../requirements/non-functional.md#nfr-05)
* ADR: [ADR-002 Authentication with Keycloak](../adr/adr-002-authentication-with-keycloak.md)
