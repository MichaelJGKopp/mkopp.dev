-- Seed blog posts data
-- This migration inserts initial blog posts from the existing markdown content

-- Create a default author if not exists (for blog posts)
INSERT INTO app_user.users (id, username, email, first_name, last_name, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'michael.kopp', 'michaeljg.kopp@gmail.com', 'Michael', 'Kopp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;
-- Blog Post 1: Implementing Authentication in Angular with Keycloak JS: A Complete Guide
INSERT INTO blog.blog_posts (id, slug, title, description, content, author_id, published_at, created_at, updated_at, type)
VALUES (
    'c9fd7c67-a009-4aeb-a6b3-9198f4d174a5',
    '007-keycloak-js-angular-auth',
    'Implementing Authentication in Angular with Keycloak JS: A Complete Guide',
    'Learn how to integrate Keycloak authentication into Angular using the official keycloak-js adapter with route guards, HTTP interceptors, and role-based access control.',
    '# Implementing Authentication in Angular with Keycloak JS: A Complete Guide

In this article, we''ll explore how to integrate Keycloak authentication into an Angular application using the official **keycloak-js** adapter. We''ll cover the complete implementation used in this project, from initialization to protected routes and HTTP interceptors.

## Why Keycloak JS?

[Keycloak](https://www.keycloak.org/) is a powerful open-source Identity and Access Management solution. While there''s a community wrapper called `keycloak-angular`, we chose to use the official `keycloak-js` adapter directly. As detailed in our [ADR on choosing Keycloak JS over Angular-Keycloak](https://github.com/MichaelJGKopp/mkopp.dev/blob/main/docs/adr/0007-keycloak-js-over-angular-keycloak.md), this gives us:

- **Direct control** and full visibility
- **Framework-agnostic** knowledge
- **Official support** from the Keycloak team
- **Simpler dependencies** and fewer breaking changes

🔗 [Official Keycloak JavaScript Adapter Documentation](https://www.keycloak.org/docs/latest/securing_apps/#_javascript_adapter)

## Installation

First, install the Keycloak JS adapter:

```bash
npm install keycloak-js
```

🔗 [keycloak-js on NPM](https://www.npmjs.com/package/keycloak-js)

## Project Structure

Our authentication implementation consists of four main parts:

1. **AuthService** - Manages Keycloak instance and core authentication logic
2. **HTTP Interceptor** - Adds JWT tokens to API requests
3. **Route Guards** - Protects routes requiring authentication
4. **App Initialization** - Initializes Keycloak before the app starts

## Step 1: Create the Auth Service

The `AuthService` is the core of our authentication system:

```typescript
// src/app/shared/auth/auth.service.ts
import { Injectable, signal } from ''@angular/core'';
import Keycloak from ''keycloak-js'';
import { environment } from ''../../../environments/environment'';

@Injectable({
  providedIn: ''root''
})
export class AuthService {
  private keycloakInstance!: Keycloak;
  
  // Signals for reactive state
  isAuthenticated = signal<boolean>(false);
  userProfile = signal<Keycloak.KeycloakProfile | null>(null);

  /**
   * Initialize Keycloak instance
   */
  async initialize(): Promise<boolean> {
    this.keycloakInstance = new Keycloak({
      url: environment.keycloakUrl,
      realm: environment.keycloakRealm,
      clientId: environment.keycloakClientId
    });

    try {
      const authenticated = await this.keycloakInstance.init({
        onLoad: ''check-sso'',
        silentCheckSsoRedirectUri: window.location.origin + ''/assets/silent-check-sso.html'',
        checkLoginIframe: false,
        pkceMethod: ''S256'' // Enable PKCE for better security
      });

      this.isAuthenticated.set(authenticated);

      if (authenticated) {
        await this.loadUserProfile();
      }

      // Token refresh setup
      this.setupTokenRefresh();

      return authenticated;
    } catch (error) {
      console.error(''Keycloak initialization failed:'', error);
      return false;
    }
  }

  /**
   * Login user
   */
  login(): Promise<void> {
    return this.keycloakInstance.login();
  }

  /**
   * Logout user
   */
  logout(): Promise<void> {
    this.isAuthenticated.set(false);
    this.userProfile.set(null);
    return this.keycloakInstance.logout();
  }

  /**
   * Get current access token
   */
  getToken(): string | undefined {
    return this.keycloakInstance.token;
  }

  /**
   * Update token if it''s about to expire
   */
  async updateToken(minValidity: number = 30): Promise<string | undefined> {
    try {
      const refreshed = await this.keycloakInstance.updateToken(minValidity);
      if (refreshed) {
        console.log(''Token refreshed'');
      }
      return this.keycloakInstance.token;
    } catch (error) {
      console.error(''Failed to refresh token:'', error);
      await this.logout();
      return undefined;
    }
  }

  /**
   * Check if user has a specific role
   */
  hasRole(role: string): boolean {
    return this.keycloakInstance.hasRealmRole(role);
  }

  /**
   * Load user profile from Keycloak
   */
  private async loadUserProfile(): Promise<void> {
    try {
      const profile = await this.keycloakInstance.loadUserProfile();
      this.userProfile.set(profile);
    } catch (error) {
      console.error(''Failed to load user profile:'', error);
    }
  }

  /**
   * Setup automatic token refresh
   */
  private setupTokenRefresh(): void {
    // Refresh token every 30 seconds if expiring
    setInterval(() => {
      this.updateToken(60);
    }, 30000);
  }
}
```

🔗 [Keycloak JS API Reference](https://www.keycloak.org/docs/latest/securing_apps/#javascript-adapter-reference)

### Key Methods Explained

- **`init()`**: Initializes the Keycloak client with configuration options
  - `onLoad: ''check-sso''`: Checks if user is already logged in without forcing login
  - `silentCheckSsoRedirectUri`: URL for silent SSO checks (iframe-based)
  - `pkceMethod: ''S256''`: Enables Proof Key for Code Exchange for enhanced security

- **`updateToken(minValidity)`**: Refreshes the token if it expires in less than `minValidity` seconds
- **`hasRealmRole(role)`**: Checks if the user has a specific role

🔗 [Keycloak Init Options](https://www.keycloak.org/docs/latest/securing_apps/#_javascript_adapter_init_options)

## Step 2: App Initialization

We need to initialize Keycloak **before** the Angular app starts. This ensures authentication state is ready when components load.

```typescript
// src/app/app.config.ts
import { ApplicationConfig, APP_INITIALIZER } from ''@angular/core'';
import { AuthService } from ''./shared/auth/auth.service'';

export function initializeKeycloak(authService: AuthService) {
  return () => authService.initialize();
}

export const appConfig: ApplicationConfig = {
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      deps: [AuthService],
      multi: true
    },
    // ... other providers
  ]
};
```

The `APP_INITIALIZER` token ensures Keycloak is initialized before the app bootstraps.

🔗 [Angular APP_INITIALIZER](https://angular.dev/api/core/APP_INITIALIZER)

## Step 3: HTTP Interceptor

Add JWT tokens to all API requests automatically:

```typescript
// src/app/shared/auth/auth.interceptor.ts
import { HttpInterceptorFn } from ''@angular/common/http'';
import { inject } from ''@angular/core'';
import { AuthService } from ''./auth.service'';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Only add token to API requests (not external URLs)
  if (token && req.url.includes(''/api/'')) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer $${token}`
      }
    });
    return next(clonedRequest);
  }

  return next(req);
};
```

Register the interceptor in `app.config.ts`:

```typescript
// src/app/app.config.ts
import { provideHttpClient, withInterceptors } from ''@angular/common/http'';
import { authInterceptor } from ''./shared/auth/auth.interceptor'';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([authInterceptor])
    ),
    // ... other providers
  ]
};
```

🔗 [Angular HTTP Interceptors](https://angular.dev/guide/http/interceptors)

## Step 4: Route Guards

Protect routes that require authentication:

```typescript
// src/app/shared/auth/auth.guard.ts
import { inject } from ''@angular/core'';
import { CanActivateFn, Router } from ''@angular/router'';
import { AuthService } from ''./auth.service'';

export const authGuard: CanActivateFn = async (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Redirect to login
  await authService.login();
  return false;
};

export const roleGuard = (role: string): CanActivateFn => {
  return async (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      await authService.login();
      return false;
    }

    if (!authService.hasRole(role)) {
      // User doesn''t have required role
      router.navigate([''/unauthorized'']);
      return false;
    }

    return true;
  };
};
```

Use guards in your routes:

```typescript
// src/app/app.routes.ts
import { Routes } from ''@angular/router'';
import { authGuard, roleGuard } from ''./shared/auth/auth.guard'';

export const routes: Routes = [
  {
    path: ''dashboard'',
    canActivate: [authGuard],
    loadComponent: () => import(''./dashboard/dashboard.component'')
  },
  {
    path: ''admin'',
    canActivate: [roleGuard(''ROLE_ADMIN'')],
    loadComponent: () => import(''./admin/admin.component'')
  }
];
```

🔗 [Angular Route Guards](https://angular.dev/guide/routing/guards)

## Step 5: Silent SSO Check

Create a silent SSO check HTML file for background authentication:

```html
<!-- src/assets/silent-check-sso.html -->
<!DOCTYPE html>
<html>
<head>
    <title>Silent SSO Check</title>
</head>
<body>
    <script>
        parent.postMessage(location.href, location.origin);
    </script>
</body>
</html>
```

This file enables Keycloak to silently check authentication status in an iframe.

🔗 [Keycloak Silent SSO](https://www.keycloak.org/docs/latest/securing_apps/#_silent_check_sso)

## Using Authentication in Components

Now you can use authentication in your components:

```typescript
// navbar.component.ts
import { Component, inject } from ''@angular/core'';
import { AuthService } from ''../shared/auth/auth.service'';

@Component({
  selector: ''mysite-navbar'',
  template: `
    <nav>
      @if (authService.isAuthenticated()) {
        <div>
          <span>Welcome, {{ authService.userProfile()?.firstName }}</span>
          <button (click)="authService.logout()">Logout</button>
        </div>
      } @else {
        <button (click)="authService.login()">Login</button>
      }
    </nav>
  `
})
export class NavbarComponent {
  authService = inject(AuthService);
}
```

## Environment Configuration

Configure Keycloak connection in your environment files:

```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  keycloakUrl: ''http://localhost:8080'',
  keycloakRealm: ''my-realm'',
  keycloakClientId: ''my-angular-app''
};
```

```typescript
// src/environments/environment.prod.ts
export const environment = {
  production: true,
  keycloakUrl: ''https://auth.mysite.com'',
  keycloakRealm: ''production-realm'',
  keycloakClientId: ''mysite-frontend''
};
```

## Security Best Practices

### 1. Enable PKCE

Always use PKCE (Proof Key for Code Exchange) for enhanced security:

```typescript
await this.keycloakInstance.init({
  pkceMethod: ''S256''
});
```

🔗 [OAuth 2.0 PKCE](https://oauth.net/2/pkce/)

### 2. Token Refresh

Implement automatic token refresh to keep users logged in:

```typescript
private setupTokenRefresh(): void {
  setInterval(() => {
    this.updateToken(60); // Refresh if expiring in 60 seconds
  }, 30000); // Check every 30 seconds
}
```

### 3. Secure Token Storage

keycloak-js stores tokens in memory by default (not localStorage), which is more secure against XSS attacks.

### 4. HTTPS Only in Production

Always use HTTPS in production for Keycloak and your application.

## Debugging and Troubleshooting

### Enable Keycloak Logging

```typescript
Keycloak({ enableLogging: true });
```

### Common Issues

**Issue: "Failed to initialize adapter"**

- Check Keycloak URL and realm configuration
- Verify client exists in Keycloak admin console
- Check browser console for CORS errors

**Issue: "Token refresh failed"**

- Verify refresh token settings in Keycloak client configuration
- Check token expiration times

**Issue: "Infinite redirect loop"**

- Check `onLoad` configuration
- Verify redirect URIs in Keycloak client settings

🔗 [Keycloak Troubleshooting Guide](https://www.keycloak.org/docs/latest/server_admin/#troubleshooting)

## Conclusion

By using keycloak-js directly, we have full control over our authentication flow while benefiting from Keycloak''s robust security features. This implementation is:

- **Transparent**: We see exactly what''s happening
- **Maintainable**: Direct API usage is easier to debug
- **Framework-agnostic**: Knowledge transfers to other JavaScript frameworks
- **Production-ready**: Includes token refresh, role-based access, and security best practices

The initial setup requires more code than using a wrapper, but the long-term benefits of clarity, control, and maintainability make it worthwhile.

## Further Reading

- [Official Keycloak JavaScript Adapter Documentation](https://www.keycloak.org/docs/latest/securing_apps/#_javascript_adapter)
- [Keycloak Admin Console Guide](https://www.keycloak.org/docs/latest/server_admin/)
- [Angular Authentication Best Practices](https://angular.dev/best-practices/security)
- [OAuth 2.0 and OpenID Connect Overview](https://oauth.net/2/)
- [Keycloak Blog and Tutorials](https://www.keycloak.org/blog)
',
    '00000000-0000-0000-0000-000000000001',
    '2025-11-05'::TIMESTAMPTZ,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'BLOG'
);
-- Tag: keycloak
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('10446193-93c9-4474-8643-af8fbf8e466d', 'keycloak', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'c9fd7c67-a009-4aeb-a6b3-9198f4d174a5', id FROM blog.blog_tags WHERE name = 'keycloak';
-- Tag: angular
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('cd5065c8-f788-475e-b802-65d724e5bec6', 'angular', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'c9fd7c67-a009-4aeb-a6b3-9198f4d174a5', id FROM blog.blog_tags WHERE name = 'angular';
-- Tag: authentication
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('774b439a-0932-4580-95ac-e16dae5e5e8e', 'authentication', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'c9fd7c67-a009-4aeb-a6b3-9198f4d174a5', id FROM blog.blog_tags WHERE name = 'authentication';
-- Tag: oauth2
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('e0388d13-9645-4484-8b6f-bab4d34f7696', 'oauth2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'c9fd7c67-a009-4aeb-a6b3-9198f4d174a5', id FROM blog.blog_tags WHERE name = 'oauth2';
-- Tag: jwt
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('46c8fbd4-c8fd-4369-a9b1-c9f285c4a3ed', 'jwt', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'c9fd7c67-a009-4aeb-a6b3-9198f4d174a5', id FROM blog.blog_tags WHERE name = 'jwt';
-- Tag: security
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('41242aa5-d2d7-4e64-815e-387d6bb5d07d', 'security', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'c9fd7c67-a009-4aeb-a6b3-9198f4d174a5', id FROM blog.blog_tags WHERE name = 'security';
-- Blog Post 2: Database Migrations with Flyway in Spring Boot: A Practical Guide
INSERT INTO blog.blog_posts (id, slug, title, description, content, author_id, published_at, created_at, updated_at, type)
VALUES (
    'e15bbc65-72b5-40e1-a2a2-20ae7401665a',
    '006-flyway-spring-boot',
    'Database Migrations with Flyway in Spring Boot: A Practical Guide',
    'A comprehensive guide to managing database schema migrations in Spring Boot using Flyway, covering best practices, versioning strategies, and production deployment.',
    '# Database Migrations with Flyway in Spring Boot: A Practical Guide

Database schema versioning is a critical aspect of application development that is often overlooked until it becomes a problem. In this article, we''ll explore how to use **Flyway** with Spring Boot to manage database migrations in a clean, maintainable, and production-ready way.

## What is Flyway?

[Flyway](https://flywaydb.org/) is an open-source database migration tool that brings version control to your database schema. It allows you to describe database changes as SQL scripts that are versioned and tracked, ensuring that your database schema evolves in a controlled and repeatable manner.

Think of it as **Git for your database schema**.

## Why Flyway?

While there are several database migration tools available (most notably [Liquibase](https://www.liquibase.org/)), Flyway stands out for its simplicity and SQL-first approach. As detailed in our [ADR on choosing Flyway over Liquibase](https://github.com/MichaelJGKopp/mkopp.dev/blob/main/docs/adr/0006-flyway-over-liquibase.md), we chose Flyway primarily because:

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

For PostgreSQL specifically (which we use), you''ll also need the PostgreSQL-specific Flyway dependency:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

🔗 [Official Flyway Documentation](https://flywaydb.org/documentation/usage/plugins/springboot)

### Step 2: Configure Flyway in `application.yaml`

Spring Boot auto-configures Flyway if it''s on the classpath. You can customize the configuration:

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
2. **Creates it if it doesn''t exist**
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
',
    '00000000-0000-0000-0000-000000000001',
    '2025-11-05'::TIMESTAMPTZ,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'BLOG'
);
-- Tag: flyway
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('1812d125-e94e-47dd-94be-ff7372d3a8bc', 'flyway', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'e15bbc65-72b5-40e1-a2a2-20ae7401665a', id FROM blog.blog_tags WHERE name = 'flyway';
-- Tag: spring-boot
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('046db44d-6590-4b4e-a371-5e2b1210bd68', 'spring-boot', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'e15bbc65-72b5-40e1-a2a2-20ae7401665a', id FROM blog.blog_tags WHERE name = 'spring-boot';
-- Tag: database
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('e5ce3994-1cc7-44e3-a76d-fbc7dd0ca1b5', 'database', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'e15bbc65-72b5-40e1-a2a2-20ae7401665a', id FROM blog.blog_tags WHERE name = 'database';
-- Tag: migrations
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('3cb34025-3791-47cd-a2e1-a1c3aae36859', 'migrations', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'e15bbc65-72b5-40e1-a2a2-20ae7401665a', id FROM blog.blog_tags WHERE name = 'migrations';
-- Tag: sql
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('5d3fe907-1460-4ccd-b56c-4e9d38628f9d', 'sql', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'e15bbc65-72b5-40e1-a2a2-20ae7401665a', id FROM blog.blog_tags WHERE name = 'sql';
-- Tag: postgresql
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('764b8ae6-1dfb-4fee-b5f6-150af47d5aa8', 'postgresql', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'e15bbc65-72b5-40e1-a2a2-20ae7401665a', id FROM blog.blog_tags WHERE name = 'postgresql';
-- Blog Post 3: CORS Configuration in Spring Boot: A Best Practice Guide
INSERT INTO blog.blog_posts (id, slug, title, description, content, author_id, published_at, created_at, updated_at, type)
VALUES (
    '2a9fb2c5-553b-4a58-8c6f-747cec24daea',
    '005-cors-best-practices',
    'CORS Configuration in Spring Boot: A Best Practice Guide',
    'Learn how to configure CORS in Spring Boot in a secure, flexible, and maintainable way using centralized configuration and externalized properties.',
    '# CORS Configuration in Spring Boot: A Best Practice Guide

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

* **`allowed-origins`**: In production, never include `localhost`. Override per environment using [Spring profiles](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.profile-specific). You can also inject values from environment variables (`$${CORS_ALLOWED_ORIGINS}`).
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

For a complete example of a `SecurityConfig` that uses this approach to secure an application with Keycloak, see our blog post on [A Deep Dive into the Keycloak JWT OAuth2 Implementation in this Repository](./004-keycloak-spring-boot-integration.md).

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
',
    '00000000-0000-0000-0000-000000000001',
    '2025-11-05'::TIMESTAMPTZ,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'BLOG'
);
-- Tag: spring-boot
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('658e9c4b-2d77-41f6-8eb5-61ec2bf21ccd', 'spring-boot', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2a9fb2c5-553b-4a58-8c6f-747cec24daea', id FROM blog.blog_tags WHERE name = 'spring-boot';
-- Tag: cors
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('5509b8d6-a5c0-4ded-ad2a-4c02d44f11eb', 'cors', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2a9fb2c5-553b-4a58-8c6f-747cec24daea', id FROM blog.blog_tags WHERE name = 'cors';
-- Tag: security
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('57f8417d-5afb-40e8-b4ba-6b164353d555', 'security', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2a9fb2c5-553b-4a58-8c6f-747cec24daea', id FROM blog.blog_tags WHERE name = 'security';
-- Tag: java
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('e0637477-907f-4028-98d1-b7554f9ec6a9', 'java', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2a9fb2c5-553b-4a58-8c6f-747cec24daea', id FROM blog.blog_tags WHERE name = 'java';
-- Tag: backend
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('8415021b-41bb-476d-8097-945ed03a959c', 'backend', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2a9fb2c5-553b-4a58-8c6f-747cec24daea', id FROM blog.blog_tags WHERE name = 'backend';
-- Blog Post 4: A Deep Dive into the Keycloak JWT OAuth2 Implementation
INSERT INTO blog.blog_posts (id, slug, title, description, content, author_id, published_at, created_at, updated_at, type)
VALUES (
    '1ff96a87-0167-4600-b3af-953c7cdf8806',
    '004-keycloak-spring-boot-integration',
    'A Deep Dive into the Keycloak JWT OAuth2 Implementation',
    'Detailed analysis of securing a Spring Boot backend using Keycloak with JWT for OAuth2 authentication, including role extraction and custom converters.',
    '# A Deep Dive into the Keycloak JWT OAuth2 Implementation in this Repository

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
                    issuer-uri: $${KEYCLOAK_ISSUER_URI}
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

The `KeycloakJwtAuthenticationConverter` is a crucial piece of the puzzle. It''s responsible for converting the JWT into a Spring Security `Authentication` object.

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
        log.error("Invalid JWT: Claim ''{}'' is missing or not a Map. Token subject: {}", "realm_access",
                jwtToken.getSubject());
        throw new InvalidTokenException("Claim ''" + "realm_access" + "'' is missing or not a Map.");
    }
    Map<String, Object> realmAccess = (Map<String, Object>) realmAccessClaim;

    Object rolesValue = realmAccess.get("roles");
    if (!(rolesValue instanceof Collection)) {
        log.error(
                "Invalid JWT: Property ''{}'' is missing or not a Collection within the ''{}'' claim. Token subject: {}",
                "roles", "realm_access", jwtToken.getSubject());
        throw new InvalidTokenException("Property ''" + "roles"
                + "'' is missing or not a Collection within the ''" + "realm_access" + "'' claim.");
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
',
    '00000000-0000-0000-0000-000000000001',
    '2025-11-05'::TIMESTAMPTZ,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'BLOG'
);
-- Tag: keycloak
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('d2b79aa5-2c54-4b4d-bd84-eefdbc23e297', 'keycloak', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '1ff96a87-0167-4600-b3af-953c7cdf8806', id FROM blog.blog_tags WHERE name = 'keycloak';
-- Tag: spring-boot
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('7e3a0dc3-f8a7-4b42-b359-eda8756fe024', 'spring-boot', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '1ff96a87-0167-4600-b3af-953c7cdf8806', id FROM blog.blog_tags WHERE name = 'spring-boot';
-- Tag: oauth2
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('0acc4c88-4c7a-412b-963b-88dbd8f186e2', 'oauth2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '1ff96a87-0167-4600-b3af-953c7cdf8806', id FROM blog.blog_tags WHERE name = 'oauth2';
-- Tag: jwt
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('b1690a37-abe6-4e26-aa75-ebaae3f4d821', 'jwt', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '1ff96a87-0167-4600-b3af-953c7cdf8806', id FROM blog.blog_tags WHERE name = 'jwt';
-- Tag: security
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('d6a3d68a-9c76-4a56-b653-62975d62feb3', 'security', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '1ff96a87-0167-4600-b3af-953c7cdf8806', id FROM blog.blog_tags WHERE name = 'security';
-- Tag: java
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('2258aee1-2eb5-4f26-b380-7265a892effe', 'java', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '1ff96a87-0167-4600-b3af-953c7cdf8806', id FROM blog.blog_tags WHERE name = 'java';
-- Blog Post 5: Taming the Monorepo: How We Use Nx to Manage Our Full-Stack Application
INSERT INTO blog.blog_posts (id, slug, title, description, content, author_id, published_at, created_at, updated_at, type)
VALUES (
    '2235b8a0-414b-4830-991a-31ff03b05f0d',
    '003-nx-monorepo',
    'Taming the Monorepo: How We Use Nx to Manage Our Full-Stack Application',
    'Why we chose a monorepo approach and how we use Nx to manage our Angular frontend and Spring Boot backend in a single workspace.',
    '# Taming the Monorepo: How We Use Nx to Manage Our Full-Stack Application

This article explains why we chose a monorepo approach for this project and how we use Nx to manage our full-stack application, which consists of an Angular frontend and a Spring Boot backend.

## What is a Monorepo? And Why Use One?

A monorepo is a single repository that contains the code for multiple projects. For a full-stack application, this means having the frontend and backend code in the same repository.

This approach has several benefits:

*   **Atomic Commits**: Changes to both the frontend and backend can be made in a single commit, making it easier to track and understand changes across the entire application.
*   **Code Sharing**: It''s easy to share code between the frontend and backend, or between different applications in the monorepo.
*   **Single Source of Truth**: There is one place for all the code, which simplifies dependency management and builds.

## Why Nx?

While a monorepo offers many benefits, it can also become complex to manage as the project grows. This is where Nx comes in.

Nx is a smart, extensible build framework that helps you manage monorepos. As detailed in our [ADR on using an Nx Monorepo](https://github.com/MichaelJGKopp/mkopp.dev/blob/main/docs/adr/0002-nx-monorepo.md), we chose Nx for several reasons:

*   **Excellent Tooling**: Nx provides a rich set of tools for managing monorepos, including generators for creating new applications and libraries, and executors for running tasks like building, testing, and linting.
*   **Dependency Graph**: Nx understands the dependencies between projects in the monorepo, which allows it to do smart things like only rebuilding and re-testing the parts of the application that have changed.
*   **Caching**: Nx can cache the results of builds and tests, which can significantly speed up development.
*   **Plugins**: Nx has a rich ecosystem of plugins for different technologies. In our project, we use plugins for Angular, Spring Boot, Jest, ESLint, and Playwright.

## Our Project Structure

Our Nx workspace is organized into two main folders:

*   `apps`: This folder contains our two main applications:
    *   `mysite-frontend`: The Angular SSR frontend.
    *   `mysite-backend`: The Spring Boot backend.
*   `libs`: This folder is currently empty, but it''s where we would put any shared code or libraries that might be used by both the frontend and backend in the future.

This structure provides a clear separation of concerns and makes it easy to navigate the codebase.

## Nx in Action: A Look at Our Configuration

Our Nx configuration is defined in the `nx.json` file. This file configures target defaults, plugins, and generators.

### Key Plugins

We use several Nx plugins to manage our full-stack application:

*   `@nx/angular`: Provides generators and executors for Angular development.
*   `@nxrocks/nx-spring-boot`: Integrates our Spring Boot backend into the Nx workspace, allowing us to build, test, and run it using Nx commands.
*   `@nx/jest`: For running unit tests.
*   `@nx/eslint`: For linting our code.
*   `@nx/playwright`: For end-to-end testing.

These plugins provide a consistent way to manage our projects and ensure that we follow best practices.

## Conclusion

Using an Nx monorepo has been a great choice for this project. It provides a solid foundation for our full-stack application and has allowed us to maintain a clean, organized, and scalable codebase. While some of the more advanced features of Nx might seem like overkill for a project of this size, they provide a clear path for future growth and ensure that we are following industry best practices from the start.
',
    '00000000-0000-0000-0000-000000000001',
    '2025-11-05'::TIMESTAMPTZ,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'BLOG'
);
-- Tag: nx
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('ee582faf-0d4f-4421-b401-73d337ef4931', 'nx', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2235b8a0-414b-4830-991a-31ff03b05f0d', id FROM blog.blog_tags WHERE name = 'nx';
-- Tag: monorepo
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('f08cb865-0e77-4017-80d2-c0627ff7e34f', 'monorepo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2235b8a0-414b-4830-991a-31ff03b05f0d', id FROM blog.blog_tags WHERE name = 'monorepo';
-- Tag: angular
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('91e19381-7c16-46b0-b87c-459388a3af04', 'angular', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2235b8a0-414b-4830-991a-31ff03b05f0d', id FROM blog.blog_tags WHERE name = 'angular';
-- Tag: spring-boot
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('033e1da8-847b-417c-a3bb-356b834fa9ba', 'spring-boot', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2235b8a0-414b-4830-991a-31ff03b05f0d', id FROM blog.blog_tags WHERE name = 'spring-boot';
-- Tag: full-stack
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('57ff6b66-17fe-4be4-824a-156e63db9c32', 'full-stack', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2235b8a0-414b-4830-991a-31ff03b05f0d', id FROM blog.blog_tags WHERE name = 'full-stack';
-- Blog Post 6: The Power of Server-Side Rendering with Angular Universal
INSERT INTO blog.blog_posts (id, slug, title, description, content, author_id, published_at, created_at, updated_at, type)
VALUES (
    '2e75c6d0-8fe5-4e29-be82-87c82dfe46d7',
    '002-ssr-angular',
    'The Power of Server-Side Rendering with Angular Universal',
    'How we use Angular Universal for Server-Side Rendering to enhance SEO and improve performance, including containerization with Docker.',
    '# The Power of Server-Side Rendering with Angular Universal

This article explores how we use Server-Side Rendering (SSR) with Angular Universal in this project to enhance SEO and improve performance.

## What is SSR and Why is it Important?

Server-Side Rendering (SSR) is a technique for rendering a client-side single-page application (SPA) on the server and sending a fully rendered page to the client. This is in contrast to a traditional SPA, where the browser downloads a minimal HTML page and then renders the application using JavaScript.

For a portfolio and blog site like this, SSR is crucial for two main reasons:

1.  **Search Engine Optimization (SEO)**: Search engine crawlers can more easily index a fully rendered HTML page, which improves the site''s visibility in search results.
2.  **Performance**: Users see a meaningful first paint of the application much faster, as they don''t have to wait for the JavaScript to download and execute.

As detailed in our [ADR on SSR with Angular](https://github.com/MichaelJGKopp/mkopp.dev/blob/main/docs/adr/0003-ssr-angular.md), we chose Angular Universal to get the best of both worlds: the rich interactivity of an SPA and the SEO and performance benefits of a server-rendered application.

## How SSR Works in Our Project

Our Angular application is configured to be built in two main parts:

1.  **A browser bundle**: The standard Angular application that runs in the user''s browser.
2.  **A server bundle**: A version of the application that can be run on a Node.js server.

When a user requests a page, our Node.js server uses the server bundle to render the requested route into static HTML and sends it to the user. The browser then downloads the browser bundle in the background and "hydrates" the static HTML, taking over and turning it into a fully interactive SPA.

## The Key Files

Several files in the `apps/mysite-frontend` directory are key to our SSR implementation:

*   `server.ts`: This is the heart of our SSR setup. It''s a Node.js Express server that handles incoming requests. It uses the `@angular/ssr` library to create an Angular engine that renders the application. It also serves static files and has a health check endpoint.
*   `main.server.ts`: This is the main entry point for the server-side application. It bootstraps the Angular application using a server-specific configuration.
*   `app.config.server.ts`: This file provides the server-specific application configuration, including the `provideServerRendering` function from `@angular/ssr` which enables the SSR capabilities.

## The Build Process

The `project.json` file for our frontend application contains the build configuration for SSR. The `@angular/build:application` executor is configured with `"outputMode": "server"`, which tells the Angular CLI to produce both a browser and a server build.

When we run `npx nx build mysite-frontend`, the CLI creates a `dist/apps/mysite-frontend` directory with `browser` and `server` subdirectories, containing the respective bundles.

## Containerizing the SSR App with Docker

To run our SSR application in production, we containerize it using Docker. Our `Dockerfile.prod` is a multi-stage Dockerfile that is optimized for security and a small image size.

Here are the key aspects of our Dockerfile:

1.  **Builder Stage**: We use a `node` image to build the application. We copy over the necessary files, install dependencies, and run the `nx build` command. This stage contains all the development dependencies and build tools.
2.  **Runner Stage**: We use a slim `node` image for the final production image. We create a non-root user (`nodejs`) for security. We then copy only the built application from the `builder` stage into the `runner` stage. This results in a much smaller and more secure production image.
3.  **Running the server**: The `CMD` instruction starts the Node.js server, which in turn serves our server-rendered Angular application.

## Conclusion

By using Angular Universal for Server-Side Rendering, we get a fast, SEO-friendly, and modern web application. The setup is robust, and the use of Docker allows us to deploy it consistently and securely. This approach is a great example of how to build enterprise-grade Angular applications that are both performant and discoverable.
',
    '00000000-0000-0000-0000-000000000001',
    '2025-11-05'::TIMESTAMPTZ,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'BLOG'
);
-- Tag: angular
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('febc764a-0f22-4441-97e4-262e4d6bc4b5', 'angular', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2e75c6d0-8fe5-4e29-be82-87c82dfe46d7', id FROM blog.blog_tags WHERE name = 'angular';
-- Tag: ssr
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('7f2248a2-aed2-4efe-b05e-9d40075d9967', 'ssr', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2e75c6d0-8fe5-4e29-be82-87c82dfe46d7', id FROM blog.blog_tags WHERE name = 'ssr';
-- Tag: angular-universal
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('b3027d8e-b46a-4e30-b961-23d97c6e5fb0', 'angular-universal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2e75c6d0-8fe5-4e29-be82-87c82dfe46d7', id FROM blog.blog_tags WHERE name = 'angular-universal';
-- Tag: seo
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('beed8bbb-fbf7-400d-8126-86f28b0eb810', 'seo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2e75c6d0-8fe5-4e29-be82-87c82dfe46d7', id FROM blog.blog_tags WHERE name = 'seo';
-- Tag: performance
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('414d9bf8-6dab-4240-a019-79a6869c0fed', 'performance', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2e75c6d0-8fe5-4e29-be82-87c82dfe46d7', id FROM blog.blog_tags WHERE name = 'performance';
-- Tag: docker
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('cd166ee3-6913-4cfc-8be8-d899b0530fed', 'docker', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '2e75c6d0-8fe5-4e29-be82-87c82dfe46d7', id FROM blog.blog_tags WHERE name = 'docker';
-- Blog Post 7: Our Deployment Pipeline: From git push to Production
INSERT INTO blog.blog_posts (id, slug, title, description, content, author_id, published_at, created_at, updated_at, type)
VALUES (
    'a53c5054-fb02-4c36-90e1-ab7429f0b176',
    '001-deployment-pipeline',
    'Our Deployment Pipeline: From git push to Production',
    'A deep dive into our CI/CD pipeline using GitHub Actions, Docker, and Docker Compose to automate deployment from code push to production.',
    '# Our Deployment Pipeline: From `git push` to Production

This post provides a deep dive into the CI/CD and deployment pipeline used for this project. It''s designed to be automated, reliable, and to follow industry best practices.

## The Big Picture: A High-Level Overview

Our deployment pipeline is triggered on every push to the `main` branch. It consists of three main stages:

1. **Build & Test**: The application is built and tested in a clean environment.
2. **Publish Docker Images**: The frontend and backend applications are packaged into Docker images and pushed to a container registry.
3. **Deploy to VPS**: The new images are pulled on the production server, and the services are updated.

Here is a visual representation of the pipeline:

```mermaid
graph TD
    A[Push to main] --> B{Build & Test};
    B --> C{Publish Docker Images};
    C --> D{Deploy to VPS};

    subgraph GitHub Actions
        A
        B
        C
    end

    subgraph VPS
        D
    end
```

## The CI Stage: Building and Pushing with GitHub Actions

The continuous integration part of our pipeline is handled by GitHub Actions. The workflow is defined in `.github/workflows/deploy.yml`.

### Build and Push Jobs

The workflow has two parallel jobs, `build-and-push-frontend` and `build-and-push-backend`, which are responsible for building the Docker images for the frontend and backend respectively.

These jobs perform the following steps:

1. **Checkout the code**.
2. **Set up Docker Buildx** for building multi-platform images.
3. **Log in to Docker Hub** using secrets stored in the repository.
4. **Extract metadata** such as tags and labels for the Docker images.
5. **Build and push the Docker image** to Docker Hub. The images are tagged with the short commit SHA for traceability.

### The Deploy Job

Once the images are successfully built and pushed, the `deploy` job is triggered. This job is responsible for updating the services on our production Virtual Private Server (VPS).

It performs the following steps:

1. **Sets up an SSH agent** with the private key to access the VPS.
2. **Adds the remote host to `known_hosts`** to avoid interactive prompts.
3. **Connects to the VPS via SSH and executes the `deploy.sh` script**.

## The Deployment Stage: Updating the Services on the VPS

The `deploy.sh` script on the server orchestrates the update of the running services. It uses `docker-compose` with the `docker/docker-compose.prod.yml` file to manage the services.

Here is the core logic of the script:

1. **It expects `FRONTEND_TAG` and `BACKEND_TAG` environment variables** to be set by the GitHub Actions workflow.
2. **It updates the backend service first** using `docker compose ... up -d --force-recreate backend`.
3. **It waits for 30 seconds** for the backend to stabilize.
4. **It then updates the frontend service**.
5. **Finally, it prunes old Docker images** to save disk space.

This backend-first deployment strategy helps to minimize downtime, as the frontend is only updated after the new backend is healthy and ready.

## Infrastructure and Architecture

Our production environment is running on a VPS and is orchestrated using Docker Compose. The key components are:

* **Traefik**: A modern reverse proxy that handles HTTPS termination and routing to the frontend and backend services.
* **PostgreSQL**: The database for our backend.
* **Frontend and Backend services**: Running as Docker containers.

For more details on the architecture and the decisions behind it, you can refer to our [ADR on Deployment with Docker and Traefik](https://github.com/MichaelJGKopp/mkopp.dev/blob/main/docs/adr/0004-deployment-with-docker-and-traefik.md).

## Limitations and Future Improvements

Our current pipeline is robust, but there is always room for improvement. Here are some of the current limitations and potential future enhancements:

* **No Zero-Downtime Deployment**: The current setup stops the old containers before starting the new ones, which can cause a brief downtime. In the future, we might move to a more advanced orchestration tool like Kubernetes to achieve zero-downtime deployments.
* **Nx Integration**: Currently, Nx is only used to build the frontend. We plan to integrate the backend build process into Nx as well.
* **Linting and Testing**: We plan to integrate linting and more comprehensive testing into the CI/CD pipeline to improve code quality.

## Conclusion

Our current deployment pipeline provides a solid foundation for developing and deploying our application. It''s automated, reliable, and follows modern DevOps practices. It allows us to ship new features and bug fixes to production quickly and safely.
',
    '00000000-0000-0000-0000-000000000001',
    '2025-11-05'::TIMESTAMPTZ,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'BLOG'
);
-- Tag: ci-cd
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('dbed5cf6-a282-4c29-a7c0-044e96c8cca3', 'ci-cd', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'a53c5054-fb02-4c36-90e1-ab7429f0b176', id FROM blog.blog_tags WHERE name = 'ci-cd';
-- Tag: github-actions
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('e4143dc0-0d15-40d8-9c91-bbe1632d2091', 'github-actions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'a53c5054-fb02-4c36-90e1-ab7429f0b176', id FROM blog.blog_tags WHERE name = 'github-actions';
-- Tag: docker
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('33125395-7b17-4eeb-8bb0-1f680dcc6e80', 'docker', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'a53c5054-fb02-4c36-90e1-ab7429f0b176', id FROM blog.blog_tags WHERE name = 'docker';
-- Tag: deployment
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('8d6bfb9c-41eb-4e6e-b7ed-9964045c067c', 'deployment', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'a53c5054-fb02-4c36-90e1-ab7429f0b176', id FROM blog.blog_tags WHERE name = 'deployment';
-- Tag: devops
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('4de2de7d-94c0-424c-baa3-cb31d9533d20', 'devops', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT 'a53c5054-fb02-4c36-90e1-ab7429f0b176', id FROM blog.blog_tags WHERE name = 'devops';
