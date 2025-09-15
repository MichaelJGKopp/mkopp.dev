# Test Plan: <Feature / Module Name>

## 1. Introduction

- **Feature/Module**: <Name>
- **Author**: <Name>
- **Date**: <YYYY-MM-DD>
- **Version**: <vX.Y>

## 2. Scope

- What is being tested?
- What is **out of scope**?

## 3. Test Strategy

- **Levels**: Unit, Integration, E2E, Performance
- **Approach**: TDD, BDD, exploratory, automated/manual mix
- **Tools**: JUnit, Cucumber, Cypress, Postman, etc.

## 4. Test Environment

- Hardware/VMs
- OS
- Dependencies (DB, Auth, APIs)
- Configurations

## 5. Test Cases

| ID  | Description                | Preconditions | Steps | Expected Result |
|-----|----------------------------|---------------|-------|-----------------|
| TC1 | User can log in as Admin   | DB seeded     | 1. Open login <br> 2. Enter creds | Admin dashboard visible |

## 6. Risks & Mitigations

- Example: “Keycloak downtime → Mitigation: mock provider in tests”

## 7. Schedule

- Sprint or release when tests will be executed

## 8. Approval

- Sign-off: <Name / Role>
