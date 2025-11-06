# ADR 0007: Use Keycloak JS Over Angular-Keycloak

## Status

Accepted

## Context

For integrating Keycloak authentication into our Angular frontend, we had two primary options:

1. **keycloak-js** - The official Keycloak JavaScript adapter
2. **keycloak-angular** - A community-maintained Angular wrapper around keycloak-js

Both libraries provide authentication and authorization capabilities, but they differ significantly in their approach and implementation.

## Decision

We have decided to use **keycloak-js** directly instead of the keycloak-angular wrapper.

## Rationale

### 1. Direct Control and Transparency

Using keycloak-js directly gives us complete visibility into how authentication works:

- **No Hidden Abstractions**: We can see exactly what methods are being called
- **Clear Debugging**: Stack traces and errors point directly to Keycloak code
- **Full API Access**: We have access to all Keycloak JS methods, not just what the wrapper exposes
- **Better Understanding**: Team members learn the actual Keycloak API, not a wrapper's API

With keycloak-angular, there's an additional layer of abstraction that can obscure what's happening under the hood, making debugging and customization more difficult.

### 2. Framework Agnostic Knowledge

keycloak-js works with any JavaScript framework:

- **Transferable Skills**: Knowledge gained is applicable to React, Vue, Svelte, or vanilla JS projects
- **Consistent API**: If we migrate to another framework, authentication code requires minimal changes
- **Industry Standard**: keycloak-js is the official adapter used across the industry
- **Better Documentation**: Official Keycloak docs focus on keycloak-js

Learning keycloak-angular means learning Angular-specific patterns that don't translate to other frameworks.

### 3. Maintenance and Updates

keycloak-js is maintained by the Keycloak team:

- **Official Support**: Maintained by Red Hat/Keycloak core team
- **Guaranteed Compatibility**: Always compatible with latest Keycloak versions
- **Timely Updates**: Security patches and new features arrive first
- **Long-term Stability**: Won't be abandoned or deprecated

keycloak-angular is a community wrapper that:

- May lag behind keycloak-js updates
- Could be abandoned if maintainers lose interest
- Might have Angular version compatibility issues

### 4. Simpler Dependency Chain

Using keycloak-js directly:

- **One Dependency**: Just `keycloak-js`
- **Smaller Bundle**: No wrapper code
- **Fewer Breaking Changes**: Only track keycloak-js updates

Using keycloak-angular:

- **Two Dependencies**: Both `keycloak-js` and `keycloak-angular`
- **Version Compatibility Issues**: Need to ensure keycloak-angular version matches both keycloak-js and Angular versions
- **More Breaking Changes**: Breaks when either dependency has breaking changes

### 5. Modern Angular Compatibility

With Angular 14+ introducing standalone components and the inject() function, modern Angular code is less reliant on Angular-specific patterns anyway. We can easily integrate keycloak-js using:

- **Standalone Services**: No need for Angular modules
- **Functional Guards**: Clean integration with route guards
- **Signals**: Reactive state management without RxJS complexity

### 6. Better Error Handling

With direct keycloak-js usage:

```typescript
try {
  const authenticated = await keycloak.init({ onLoad: 'login-required' });
  console.log('Authentication successful:', authenticated);
} catch (error) {
  console.error('Keycloak initialization failed:', error);
  // Clear error message, direct from Keycloak
}
```

With a wrapper, errors pass through an additional layer, making them harder to diagnose.

## Implementation Approach

Our implementation uses modern Angular patterns:

```typescript
// auth.service.ts
@Injectable({ providedIn: 'root' })
export class AuthService {
  private keycloak!: Keycloak;
  
  async initialize(): Promise<boolean> {
    this.keycloak = new Keycloak({
      url: environment.keycloakUrl,
      realm: environment.keycloakRealm,
      clientId: environment.keycloakClientId
    });
    
    return await this.keycloak.init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html'
    });
  }
  
  login() {
    return this.keycloak.login();
  }
  
  logout() {
    return this.keycloak.logout();
  }
  
  getToken(): string | undefined {
    return this.keycloak.token;
  }
}
```

This is straightforward, transparent, and maintainable.

## Consequences

### Positive

- Full control over authentication flow
- Direct access to all Keycloak features
- Knowledge transferable to other frameworks
- Official support and guaranteed compatibility
- Simpler dependency management
- Clearer debugging and error handling
- Better long-term maintainability

### Negative

- Need to write some Angular integration code ourselves (guards, interceptors)
- No Angular-specific conveniences from keycloak-angular
- Slightly more initial setup code

### Neutral

- Requires understanding the Keycloak JS API (valuable knowledge anyway)
- Need to handle Angular-specific patterns manually (promotes better understanding)

## Alternatives Considered

### keycloak-angular

**Pros:**

- Angular-specific conveniences
- Pre-built guards and interceptors
- Less boilerplate for Angular integration

**Cons:**

- Additional abstraction layer
- Framework-locked knowledge
- Two dependencies to manage
- Potential compatibility lag
- Community-maintained (not official)
- Less transparent behavior

## References

- [Keycloak JavaScript Adapter Documentation](https://www.keycloak.org/docs/latest/securing_apps/#_javascript_adapter)
- [keycloak-js NPM Package](https://www.npmjs.com/package/keycloak-js)
- [keycloak-angular GitHub](https://github.com/mauriciovigolo/keycloak-angular)
- [Securing Angular Applications with Keycloak](https://www.keycloak.org/securing-apps/javascript-adapter)

## Notes

This decision aligns with our architectural principle of preferring transparency and direct control over convenient abstractions (see also: ADR 0006 on choosing Flyway's SQL over Liquibase's XML).

We value understanding how our systems work over saving a few lines of configuration code.
