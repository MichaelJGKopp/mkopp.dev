# CORS Configuration in Spring Boot: A Best Practice Guide

## What is CORS and Why Does It Exist?

By default, web browsers enforce the **[Same-Origin Policy](https://developer.mozilla.org/en-US/docs/Web/Security/Same-origin_policy)** — a critical security measure that prevents scripts loaded from one origin (domain, protocol, and port) from making requests to another origin. This policy protects users from malicious scripts that could otherwise read sensitive data from other browser tabs.

**[Cross-Origin Resource Sharing (CORS)](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)** is a W3C standard that allows servers to relax this policy in a controlled way. It uses additional HTTP headers to tell a browser that a web application running at one origin has permission to access selected resources from a server at a different origin.

For any modern single-page application (SPA) with a separate backend API, a proper CORS configuration is not just helpful — it’s **mandatory**.

This guide outlines best practices for configuring CORS in a Spring Boot application in a way that is **secure, flexible, and maintainable**.

---

## The Goal: Secure, Flexible, and Externalized

Our aim is to create a CORS setup that:

1. **Is Secure** — follows the *Principle of Least Privilege* (only allow what is strictly needed).
2. **Is Centralized** — avoids scattered, inconsistent configuration across the codebase.
3. **Is Externalized** — supports different environments (dev, staging, prod) without changing code.

---

## The Wrong Way: Common Pitfalls

Before diving into best practice, let’s look at what to avoid:

* **`@CrossOrigin` on Controllers:**
  While convenient for tests and tutorials, annotating controllers decentralizes your security policy. This makes it hard to manage, audit, and maintain across environments.

  🔗 [Spring @CrossOrigin docs](https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html#webmvc-cors-controller)

* **Using Wildcards (`*`):**
  Setting `allowedOrigins = "*"` is dangerous in production. When combined with `allowCredentials = true`, it’s outright invalid and will be blocked by modern browsers. Always specify trusted origins explicitly.

---

## The Right Way: Centralized Configuration

The best practice is to **centralize your CORS policy**. Spring Boot offers robust support for this, which integrates seamlessly with Spring Security.

### Step 1: Create a Type-Safe Properties Class

Using `@ConfigurationProperties` allows you to externalize CORS settings into `application.yaml`, keeping code clean and type-safe.

```java
// src/main/java/dev/mkopp/mysite/infrastructure/primary/security/CorsProperties.java
@Data
@Component
@ConfigurationProperties(prefix = "application.cors")
public class CorsProperties {
    private List<String> allowedOrigins;
    private List<String> allowedMethods;
    private List<String> allowedHeaders;
    private List<String> exposedHeaders;
    private Boolean allowCredentials;
    private Long maxAge;
}
```

🔗 [Spring Boot @ConfigurationProperties docs](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties)

---

### Step 2: Define the `WebMvcConfigurer` Bean

We then create a centralized CORS configuration bean (@RequiredArgsConstructor is from Lombok, creates a constructor and injects the spring bean of type CorsProperties).

```java
// src/main/java/dev/mkopp/mysite/infrastructure/primary/security/CorsConfig.java
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(corsProperties.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(corsProperties.getAllowedMethods().toArray(new String[0]))
                .allowedHeaders(corsProperties.getAllowedHeaders().toArray(new String[0]))
                .exposedHeaders(corsProperties.getExposedHeaders().toArray(new String[0]))
                .allowCredentials(corsProperties.getAllowCredentials())
                .maxAge(corsProperties.getMaxAge());
    }
}
```

🔗 [Spring MVC CORS reference](https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html)

---

### Step 3: Externalize in `application.yaml`

This is where flexibility comes in: different profiles (`application-dev.yaml`, `application-prod.yaml`) can override values as needed.

```yaml
# resources/application.yaml
application:
  cors:
    allowed-origins:
      - "http://localhost:3000"
      - "http://localhost:4200"
    allowed-methods:
      - "GET"
      - "POST"
      - "PUT"
      - "PATCH"
      - "DELETE"
      - "OPTIONS"
    allowed-headers:
      - "Authorization"
      - "Content-Type"
      - "Accept"
    exposed-headers:
      - "Location"
    allow-credentials: true
    max-age: 3600
```

---

## Deep Dive: Why These Settings Matter

* **`allowed-origins`**: In production, never include `localhost`. Override per environment using [Spring profiles](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.profile-specific). You can also inject values from environment variables (`${CORS_ALLOWED_ORIGINS}`).
* **`allow-credentials: true`**: Required if your API uses cookies, OAuth2, or JWT in `Authorization` headers. Without this, the browser won’t send credentials.
* **`allowed-headers`**: Be explicit. Common must-haves: `Authorization`, `Content-Type`, `Accept`.
* **`exposed-headers`**: Browsers only expose limited headers by default. If your API uses `Location` (e.g., `201 Created`) or pagination headers like `X-Total-Count`, you must list them here.
* **`max-age`**: Defines how long the preflight `OPTIONS` response is cached by browsers. `3600` (1 hour) is a good production value. Use lower values in development if you expect frequent changes.

---

## Integration with Spring Security

A frequent question: **does this work with Spring Security?**
Yes — automatically.

If you provide a `WebMvcConfigurer` CORS setup, Spring Security will pick it up when you enable CORS with `.cors(Customizer.withDefaults())`.

```java
// SecurityConfig.java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(Customizer.withDefaults()) // picks up CorsConfig automatically
        // ... other security rules ...
        .build();
}
```

🔗 [Spring Security CORS docs](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)

---

## Conclusion

By centralizing your CORS policy with a `WebMvcConfigurer`, binding it to a type-safe `@ConfigurationProperties` class, and externalizing values in `application.yaml`, you get a configuration that is:

* **Secure** — no dangerous wildcards.
* **Maintainable** — one place to manage the entire policy.
* **Flexible** — easily adapted per environment via YAML profiles or environment variables.

This approach represents the industry-standard best practice for handling CORS in enterprise Spring Boot applications.

---

### Further Reading

* [MDN Web Docs on CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
* [Spring Framework CORS Reference](https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html)
* [Spring Boot @ConfigurationProperties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties)
* [Spring Security CORS Support](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)
