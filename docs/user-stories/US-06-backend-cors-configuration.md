# User Story: Backend CORS Configuration (US-06)

## Description

As a **frontend developer**, I want the Spring Boot backend to have a proper CORS policy so that my Angular application can securely communicate with the API from a different origin (e.g., `localhost:4200` -> `localhost:8200`).

This is a foundational security feature required for the frontend and backend to interact in a decoupled architecture.

## Acceptance Criteria

* A centralized CORS configuration is implemented in the backend.
* All CORS settings (origins, methods, headers) are externalized into `application.yaml`.
* The configuration supports different `allowed-origins` for `dev` and `prod` Spring profiles.
* The Angular frontend running on `localhost:4200` can successfully make API calls to the backend in a local Docker Compose environment.
* The production configuration only allows requests from the production domain(s).
* The implementation correctly integrates with the existing Spring Security filter chain.

## Implementation Plan

1. **Create a Type-Safe Properties Class:** Create `CorsProperties.java` annotated with `@ConfigurationProperties`.
2. **Create a Centralized `WebMvcConfigurer`:** Create `CorsConfig.java` to define the global CORS policy using the properties.
3. **Externalize Configuration in YAML:** Configure `application.cors` in `application.yaml` and override `allowed-origins` in `application-dev.yaml` and `application-prod.yaml`.
4. **Integrate with Spring Security:** Ensure `SecurityConfig.java` uses `.cors(Customizer.withDefaults())` to automatically apply the configuration.

## References

* **Functional Requirement:** FR-23: Cross-Origin Resource Sharing
* **Non-Functional Requirement:** NFR-19: Environment-Specific Configuration
* **ADR:** ADR-001 – Authentication with Keycloak

## Labels

* `backend`
* `feature`
* `security`
