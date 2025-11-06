# ADR 0006: Use Flyway for Database Migrations Over Liquibase

## Status

Accepted

## Context

Database schema versioning and migration is a critical aspect of application development. We need a tool that allows us to:

- Version control database schema changes
- Apply migrations automatically during application startup
- Rollback changes if needed
- Work seamlessly with Spring Boot
- Be transparent and maintainable for the development team

The two most popular options in the Java ecosystem are:

1. **Flyway** - SQL-based migration tool
2. **Liquibase** - XML/YAML/JSON-based migration tool with SQL support

## Decision

We have decided to use **Flyway** for database migrations in this project.

## Rationale

### 1. Direct SQL Development

Flyway's primary format is **raw SQL**, which provides several advantages:

- **Transparency**: Developers can see exactly what SQL will be executed
- **Learning**: Team members improve their SQL skills by writing actual SQL
- **Debugging**: Easy to test migrations in a SQL client before applying them
- **No Abstraction Tax**: No need to learn XML/YAML syntax or Liquibase changesets
- **IDE Support**: Full SQL syntax highlighting, autocomplete, and validation in any IDE

**Liquibase**, while supporting SQL, primarily uses XML/YAML/JSON formats that abstract away the actual SQL. This adds a layer of indirection that can make debugging and understanding migrations more difficult.

Example Flyway migration:

```sql
-- V1__create_users_table.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

Equivalent Liquibase (XML):

```xml
<changeSet id="1" author="developer">
    <createTable tableName="users">
        <column name="id" type="BIGINT" autoIncrement="true">
            <constraints primaryKey="true"/>
        </column>
        <column name="username" type="VARCHAR(255)">
            <constraints nullable="false" unique="true"/>
        </column>
        <column name="email" type="VARCHAR(255)">
            <constraints nullable="false" unique="true"/>
        </column>
        <column name="created_at" type="TIMESTAMP" defaultValueComputed="NOW()">
            <constraints nullable="false"/>
        </column>
    </createTable>
</changeSet>
```

The SQL version is more concise, readable, and directly executable.

### 2. Simplicity and Convention

Flyway follows a simple naming convention:

- `V{version}__{description}.sql` for versioned migrations
- `R__{description}.sql` for repeatable migrations

This is immediately understandable without reading documentation.

### 3. Spring Boot Integration

Both Flyway and Liquibase integrate well with Spring Boot, but Flyway's integration is simpler:

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

### 4. Performance

Flyway is generally faster and lighter weight than Liquibase, with lower memory overhead. For our use case, this difference is minor but appreciated.

### 5. Community and Ecosystem

Both tools have strong communities, but Flyway has:

- Simpler documentation
- More Stack Overflow answers
- Clearer error messages

## Consequences

### Positive

- Developers write and understand actual SQL
- Migrations are transparent and easy to review in code reviews
- Lower learning curve for new team members familiar with SQL
- Better control over database-specific features and optimizations
- Easier debugging and testing of migrations

### Negative

- Less database abstraction (but we're already committed to PostgreSQL)
- Rollback requires writing explicit down migrations (V scripts)
- Some advanced Liquibase features (like preconditions) require more manual work

### Neutral

- Need to maintain SQL syntax knowledge (which is valuable anyway)
- Database-specific syntax means migrations aren't portable (acceptable for this project)

## Alternatives Considered

### Liquibase

**Pros:**

- Database-agnostic changesets
- Rich set of preconditions and contexts
- Automatic rollback generation for simple changes
- Supports multiple formats (XML, YAML, JSON, SQL)

**Cons:**

- Additional abstraction layer obscures actual SQL
- XML/YAML syntax has learning curve
- More complex configuration
- Harder to debug and test migrations
- Auto-generated SQL can be suboptimal

## References

- [Flyway Official Documentation](https://flywaydb.org/documentation/)
- [Spring Boot Flyway Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
- [Flyway vs Liquibase Comparison](https://www.liquibase.com/liquibase-vs-flyway)
- [Baeldung: Database Migrations with Flyway](https://www.baeldung.com/database-migrations-with-flyway)

## Notes

This decision aligns with our preference for transparency and direct control over our application's behavior, as seen in our other architectural decisions (e.g., choosing Keycloak JS over Angular-Keycloak).
