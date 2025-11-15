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

```
V{version}__{description}.sql
```

For example:

```
V1__create_users_table.sql
V2__add_email_to_users.sql
V3__create_posts_table.sql
```

### Migration File Structure

Create a directory structure:

```
src/main/resources/
└── db/
    └── migration/
        ├── V1__create_users_table.sql
        ├── V2__add_email_to_users.sql
        └── V3__create_posts_table.sql
```

### Example Migration: Creating a Users Table

```sql
-- V1__create_users_table.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
```

### Example Migration: Adding a Column

```sql
-- V2__add_email_to_users.sql
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20);

CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone_number);
```

🔗 [Flyway SQL-based Migrations](https://flywaydb.org/documentation/concepts/migrations#sql-based-migrations)

## How Flyway Works

When your Spring Boot application starts, Flyway:

1. **Checks for the schema history table** (`flyway_schema_history` by default)
2. **Creates it if it doesn't exist**
3. **Scans** the migration locations for SQL files
4. **Compares** available migrations with applied migrations in the history table
5. **Executes** new migrations in order
6. **Records** each successful migration in the history table

This ensures that:

- Migrations are applied **exactly once**
- Migrations are applied **in order**
- The database schema is **reproducible** across environments

## Migration Versioning Strategy

Flyway supports two types of migrations:

### 1. Versioned Migrations (V)

Used for schema changes:

```
V1__initial_schema.sql
V2__add_user_roles.sql
V3__add_audit_columns.sql
```

Versioned migrations:

- Are applied **once**
- Must have **unique version numbers**
- Are applied **in order**
- **Cannot be modified** after being applied

### 2. Repeatable Migrations (R)

Used for views, procedures, and functions:

```
R__create_user_summary_view.sql
R__update_statistics_procedure.sql
```

Repeatable migrations:

- Are **re-applied** when their checksum changes
- Are applied **after** all versioned migrations
- Are useful for **database objects** that can be recreated

🔗 [Flyway Migration Types](https://flywaydb.org/documentation/concepts/migrations#migration-types)

## Best Practices

### 1. Never Modify Applied Migrations

Once a migration is applied in production, **never change it**. Instead, create a new migration to fix issues.

❌ **Wrong:**

```sql
-- V1__create_users.sql (modified after being applied)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(500) NOT NULL -- Changed from 255 to 500
);
```

✅ **Correct:**

```sql
-- V2__increase_username_length.sql
ALTER TABLE users 
ALTER COLUMN username TYPE VARCHAR(500);
```

### 2. Use Descriptive Names

```
✅ V1__create_users_table.sql
✅ V2__add_email_index_to_users.sql
❌ V1__update.sql
❌ V2__fix.sql
```

### 3. Keep Migrations Small and Focused

Each migration should do **one thing** well. This makes rollbacks and debugging easier.

### 4. Test Migrations Locally First

Always test migrations in a local or dev environment before applying to production.

### 5. Use Transactions Where Possible

PostgreSQL supports DDL transactions, so use them:

```sql
-- V3__complex_migration.sql
BEGIN;

CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    title VARCHAR(500) NOT NULL,
    content TEXT
);

CREATE INDEX idx_posts_user_id ON posts(user_id);

COMMIT;
```

If any statement fails, the entire migration is rolled back.

🔗 [PostgreSQL Transactional DDL](https://www.postgresql.org/docs/current/ddl.html)

## Handling Production Databases

For existing production databases, use `baseline-on-migrate`:

```yaml
spring:
  flyway:
    baseline-on-migrate: true
    baseline-version: 0
```

This tells Flyway: "This database already exists, start tracking from now."

Then your first migration becomes `V1__...`, and Flyway applies it and all future migrations.

## Rollback Strategy

Flyway Community Edition **does not** support automatic rollbacks. For rollbacks, you have two options:

### Option 1: Manual Rollback Migrations

Create down migrations manually:

```sql
-- V4__drop_posts_table.sql (undo V3)
DROP TABLE IF EXISTS posts;
```

### Option 2: Database Backups

Maintain regular backups and restore if needed. This is the recommended approach for production.

🔗 [Flyway Undo Migrations (Teams Edition)](https://flywaydb.org/documentation/concepts/migrations#undo-migrations)

## Monitoring and Troubleshooting

### Check Migration Status

Flyway provides command-line tools:

```bash
./mvnw flyway:info
```

### View Schema History

Query the tracking table:

```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

### Common Issues

**Issue: "Checksum mismatch"**

- **Cause**: Migration file was modified after being applied
- **Solution**: Never modify applied migrations. Create a new migration or use `flyway:repair` (carefully!)

**Issue: "Migration failed"**

- **Cause**: SQL error in migration
- **Solution**: Fix the SQL, delete the failed entry from `flyway_schema_history`, and retry

🔗 [Flyway Troubleshooting](https://flywaydb.org/documentation/troubleshooting)

## Integration with Spring Boot Profiles

You can have environment-specific migrations:

```
src/main/resources/
└── db/
    └── migration/
        ├── common/
        │   └── V1__create_users_table.sql
        └── dev/
            └── V100__insert_test_data.sql
```

Configure in `application-dev.yaml`:

```yaml
spring:
  flyway:
    locations: classpath:db/migration/common,classpath:db/migration/dev
```

## Conclusion

Flyway provides a robust, production-ready solution for database schema versioning in Spring Boot applications. Its SQL-first approach ensures transparency, and its seamless Spring Boot integration makes it effortless to use.

By following best practices and understanding how Flyway works, you can maintain a clean, versioned database schema that evolves safely alongside your application code.

## Further Reading

- [Official Flyway Documentation](https://flywaydb.org/documentation/)
- [Spring Boot Database Initialization](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization)
- [Baeldung: Database Migrations with Flyway](https://www.baeldung.com/database-migrations-with-flyway)
- [Flyway Tutorial for Beginners](https://flywaydb.org/documentation/getstarted/firststeps/maven)
