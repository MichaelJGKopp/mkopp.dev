# A Deep Dive into the Keycloak JWT OAuth2 Implementation in this Repository

This guide provides a detailed analysis of how the backend of this project (`mysite-backend`) is secured using Keycloak with JWT for OAuth2 authentication. It serves as documentation for the existing implementation.

## Introduction

[Keycloak](https://www.keycloak.org/) is an open-source identity and access management solution that makes it easy to secure applications and services with little to no code. [Spring Boot](https://spring.io/projects/spring-boot) is a popular framework for building stand-alone, production-grade Spring-based applications.

This article will walk you through the key parts of the Spring Boot and Keycloak integration in this repository.

## Prerequisites

To understand this guide, you should be familiar with:

*   Java 17
*   Spring Boot
*   Maven
*   OAuth2 and JWT concepts

## Spring Boot Project Setup

The `mysite-backend` application is a Maven project. The necessary dependencies for OAuth2 resource server support are already included in the `pom.xml` file:

```xml
<!-- authentication -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

This dependency, along with `spring-boot-starter-web` and `spring-boot-starter-security`, provides all the necessary tools to configure a secure resource server.

## Configuring the Application

The application is configured in `src/main/resources/application.yaml`. The Keycloak issuer URI is configured using an environment variable to avoid hardcoding secrets:

```yaml
spring:
    security:
        oauth2:
            resourceserver:
                jwt:
                    issuer-uri: ${KEYCLOAK_ISSUER_URI}
```

This is a good practice for security and flexibility, as the issuer URI can be changed for different environments (dev, prod) without modifying the codebase.

## Security Configuration

The core of the security implementation is in the `SecurityConfig` class, located at `src/main/java/dev/mkopp/mysite/wire/crosscutting/security/SecurityConfig.java`.

### SecurityFilterChain

The `SecurityFilterChain` bean configures the security rules for the application:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter) throws Exception {
        http
                .csrf(crsf -> crsf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints for documentation (available in dev)
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()

                        // Public actuator endpoints for health checks
                        .requestMatchers("/management/health/**")
                        .permitAll()

                        // Secure management endpoints to be accessible only by ADMIN
                        .requestMatchers("/management/**")
                        .hasRole("ADMIN")

                        // All other API requests must be authenticated
                        .requestMatchers("/api/**").authenticated()

                        // Deny any other request by default for security
                        .anyRequest().denyAll())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        // This is critical for extracting roles from the Keycloak JWT
        return new KeycloakJwtAuthenticationConverter();
    }
}
```

Key aspects of this configuration:
- **CSRF is disabled**, which is common for stateless REST APIs.
- **CORS is enabled** with default settings. The line `.cors(Customizer.withDefaults())` enables CORS support. For a detailed guide on how to configure CORS in a secure and flexible way, see our blog post on [CORS Configuration in Spring Boot: A Best Practice Guide](./005-cors-best-practices.md).
- **Specific endpoints are made public** for documentation and health checks.
- **Management endpoints are secured** to be accessible only by users with the `ADMIN` role.
- **All other API endpoints require authentication.**
- **Session management is stateless**, which is essential for a resource server.
- A **custom `jwtAuthenticationConverter`** is used to process the JWT.

### JWT Authentication Converter

The `KeycloakJwtAuthenticationConverter` is a crucial piece of the puzzle. It's responsible for converting the JWT into a Spring Security `Authentication` object.

Here is the code from `src/main/java/dev/mkopp/mysite/shared/authentication/infrastructure/primary/KeycloakJwtAuthenticationConverter.java`:

```java
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt source) {
        Collection<? extends GrantedAuthority> jwtAuthorities = new JwtGrantedAuthoritiesConverter().convert(source);
        return new JwtAuthenticationToken(source,
                Stream.concat(
                        jwtAuthorities != null ? jwtAuthorities.stream() : Stream.empty(),
                        extractResourceRoles(source).stream())
                        .collect(Collectors.toSet()));
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        return AuthenticatedUser.extractRolesFromToken(jwt).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
```

This converter uses the standard `JwtGrantedAuthoritiesConverter` and also calls `AuthenticatedUser.extractRolesFromToken()` to extract custom roles from the JWT.

## Deep Dive into Role Extraction

The role extraction logic is centralized in the `AuthenticatedUser` class at `src/main/java/dev/mkopp/mysite/shared/authentication/application/AuthenticatedUser.java`.

```java
public static List<String> extractRolesFromToken(Jwt jwtToken) {
    Map<String, Object> claims = jwtToken.getClaims();

    Object realmAccessClaim = claims.get("realm_access");
    if (!(realmAccessClaim instanceof Map)) {
        log.error("Invalid JWT: Claim '{}' is missing or not a Map. Token subject: {}", "realm_access",
                jwtToken.getSubject());
        throw new InvalidTokenException("Claim '" + "realm_access" + "' is missing or not a Map.");
    }
    Map<String, Object> realmAccess = (Map<String, Object>) realmAccessClaim;

    Object rolesValue = realmAccess.get("roles");
    if (!(rolesValue instanceof Collection)) {
        log.error(
                "Invalid JWT: Property '{}' is missing or not a Collection within the '{}' claim. Token subject: {}",
                "roles", "realm_access", jwtToken.getSubject());
        throw new InvalidTokenException("Property '" + "roles"
                + "' is missing or not a Collection within the '" + "realm_access" + "' claim.");
    }

    return ((Collection<?>) rolesValue).stream()
            .filter(role -> role instanceof String)
            .map(String.class::cast)
            .filter(role -> role.contains("ROLE_"))
            .toList();
}
```

This method extracts roles from the `realm_access` claim in the JWT. It specifically looks for a `roles` property within that claim and filters for roles that contain the `ROLE_` prefix. This is a common pattern when working with Keycloak.

## Conclusion

This repository has a robust and well-structured implementation for securing a Spring Boot resource server with Keycloak. It follows best practices by externalizing configuration, using custom converters for JWT processing, and centralizing authentication-related logic.

## Documentation Sources

*   [Baeldung: Spring Security with Keycloak](https://www.baeldung.com/spring-boot-keycloak)
*   [Spring Security Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
