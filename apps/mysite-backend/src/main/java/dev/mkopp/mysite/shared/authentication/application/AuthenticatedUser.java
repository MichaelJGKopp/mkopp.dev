package dev.mkopp.mysite.shared.authentication.application;

import dev.mkopp.mysite.shared.authentication.domain.Role;
import dev.mkopp.mysite.shared.authentication.domain.Roles;
import dev.mkopp.mysite.shared.authentication.domain.UserId;
import dev.mkopp.mysite.shared.domain.exception.Assert;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is a utility class to get authenticated user information.
 * It consistently uses the user's immutable unique ID (the 'sub' claim from a
 * JWT)
 * as the principal identifier across all authentication methods.
 */
@Slf4j
public final class AuthenticatedUser {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_PROPERTY = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    private AuthenticatedUser() {
    }

    /**
     * Get the authenticated user's unique ID.
     *
     * @return The authenticated user's unique ID (UUID).
     * @throws NotAuthenticatedUserException if the user is not authenticated.
     */
    public static UserId userId() {
        return optionalUserId().orElseThrow(NotAuthenticatedUserException::new);
    }

    /**
     * Get the authenticated user's unique ID.
     *
     * @return The authenticated user's unique ID (UUID), or empty if not
     *         authenticated.
     */
    public static Optional<UserId> optionalUserId() {
       return authentication().map(AuthenticatedUser::readPrincipal).map(UserId::of);
    }

    /**
     * Reads the user's principal identifier (always the UUID) from the
     * Authentication object.
     * This method enforces consistency across different authentication types.
     *
     * @param authentication The authentication object.
     * @return The user's unique ID (UUID) as a String.
     * @throws UnknownAuthenticationException if the authentication type cannot be
     *                                        handled.
     */
    public static String readPrincipal(Authentication authentication) {
        Assert.notNull("authentication", authentication);

        if (authentication instanceof JwtAuthenticationToken token) {
            return token.getToken().getSubject();
        }

        if (authentication.getPrincipal() instanceof DefaultOidcUser oidcUser) {
            return oidcUser.getSubject();
        }

        // The UserDetailsService MUST be configured to return the UUID
        if (authentication.getPrincipal() instanceof UserDetails details) {
            return details.getUsername();
        }

        if (authentication.getPrincipal() instanceof String principal) {
            return principal;
        }

        throw new UnknownAuthenticationException();
    }

    /**
     * Extracts roles from a Keycloak JWT, ensuring the token structure is valid.
     *
     * @param jwtToken The JWT to parse.
     * @return A list of role strings.
     * @throws InvalidTokenException if the token is missing required
     *                               claims/properties.
     */
    public static List<String> extractRolesFromToken(Jwt jwtToken) {
        Map<String, Object> claims = jwtToken.getClaims();

        Object realmAccessClaim = claims.get(REALM_ACCESS_CLAIM);
        if (!(realmAccessClaim instanceof Map)) {
            log.error("Invalid JWT: Claim '{}' is missing or not a Map. Token subject: {}", REALM_ACCESS_CLAIM,
                    jwtToken.getSubject());
            throw new InvalidTokenException("Claim '" + REALM_ACCESS_CLAIM + "' is missing or not a Map.");
        }
        Map<String, Object> realmAccess = (Map<String, Object>) realmAccessClaim;

        Object rolesValue = realmAccess.get(ROLES_PROPERTY);
        if (!(rolesValue instanceof Collection)) {
            log.error(
                    "Invalid JWT: Property '{}' is missing or not a Collection within the '{}' claim. Token subject: {}",
                    ROLES_PROPERTY, REALM_ACCESS_CLAIM, jwtToken.getSubject());
            throw new InvalidTokenException("Property '" + ROLES_PROPERTY
                    + "' is missing or not a Collection within the '" + REALM_ACCESS_CLAIM + "' claim.");
        }

        return ((Collection<?>) rolesValue).stream()
                .filter(role -> role instanceof String)
                .map(String.class::cast)
                .filter(role -> role.contains(ROLE_PREFIX))
                .toList();
    }

    /**
     * Get the authenticated user roles.
     *
     * @return The authenticated user roles or empty roles if the user is not
     *         authenticated.
     */
    public static Roles roles() {
        return authentication().map(toRoles()).orElse(Roles.EMPTY);
    }

    private static Function<Authentication, Roles> toRoles() {
        return authentication -> new Roles(authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .map(Role::from).collect(Collectors.toSet()));
    }

    /**
     * Get the authenticated user token attributes.
     *
     * @return The authenticated user token attributes.
     * @throws NotAuthenticatedUserException  if the user is not authenticated.
     * @throws UnknownAuthenticationException if the authentication scheme is
     *                                        unknown.
     */
    public static Map<String, Object> attributes() {
        Authentication token = authentication().orElseThrow(NotAuthenticatedUserException::new);

        if (token instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getTokenAttributes();
        }

        throw new UnknownAuthenticationException();
    }

    private static Optional<Authentication> authentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }
}