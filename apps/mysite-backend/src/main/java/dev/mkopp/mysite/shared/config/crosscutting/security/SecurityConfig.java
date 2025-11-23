package dev.mkopp.mysite.shared.config.crosscutting.security;

import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties.Http;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;

import dev.mkopp.mysite.shared.authentication.infrastructure.primary.KeycloakJwtAuthenticationConverter;

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
                        // Swagger UI / Documentation endpoints
                        .requestMatchers("/swagger-ui/oauth2-redirect.html")
                        .permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()
                        // .hasRole("ADMIN")

                        // Public actuator endpoints for health checks
                        .requestMatchers("/management/health/**")
                        .permitAll()

                        // Secure management endpoints to be accessible only by ADMIN
                        .requestMatchers("/management/**")
                        .hasRole("ADMIN")

                        // Public API endpoints with GET access
                        .requestMatchers(HttpMethod.GET,
                                "/v1/blog/**")
                        .permitAll()

                        // All other API requests must be authenticated
                        .requestMatchers("/v1/**")
                        .authenticated()

                        // Deny any other request by default for security
                        .anyRequest().denyAll())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // .oauth2Login(Customizer.withDefaults()) // triggers browser login if needed
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
