# Database Migrations with Flyway in Spring Boot: A Practical Guide

Database schema versioning is a critical aspect of application development that is often overlooked until it becomes a problem. In this article, we'll explore how to use **Flyway** with Spring Boot to manage database migrations in a clean, maintainable, and production-ready way.

## What is Flyway?

[Flyway](https://flywaydb.org/) is an open-source database migration tool that brings version control to your database schema. It allows you to describe database changes as SQL scripts that are versioned and tracked, ensuring that your database schema evolves in a controlled and repeatable manner.

Think of it as **Git for your database schema**.

## Why Flyway?

While there are several database migration tools available (most notably [Liquibase](https://www.liquibase.org/)), Flyway stands out for its simplicity and SQL-first approach. As detailed in our [ADR on choosing Flyway over Liquibase](./../docs/adr/0006-flyway-over-liquibase.md), we chose Flyway primarily because:

1. **Direct SQL**: You write actual SQL, not XML/YAML abstractions
2. **Transparency**: You see exactly what will be executed
3. **Simplicity**: Minimal learning curve if you know SQL
4. **Spring Boot Integration**: First-class support with auto-configuration

## Adding Flyway to Your Spring Boot Project

### Step 1: Add the Dependency

Add the Flyway dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

For PostgreSQL specifically (which we use), you'll also need the PostgreSQL-specific Flyway dependency:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

🔗 [Official Flyway Documentation](https://flywaydb.org/documentation/usage/plugins/springboot)

### Step 2: Configure Flyway in `application.yaml`

Spring Boot auto-configures Flyway if it's on the classpath. You can customize the configuration:

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
    validate-on-migrate: true
```

**Key Configuration Options:**

- `enabled`: Enables Flyway (default is `true` when dependency is present)
- `locations`: Where Flyway looks for migration scripts (default: `classpath:db/migration`)
- `baseline-on-migrate`: Creates the schema history table for existing databases
- `validate-on-migrate`: Validates applied migrations against available ones

🔗 [Spring Boot Flyway Properties Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.data-migration.spring.flyway.baseline-on-migrate)

## Writing Your First Migration

Flyway migrations are SQL files with a specific naming convention:

