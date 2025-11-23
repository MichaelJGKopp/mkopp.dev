package dev.mkopp.mysite.shared.config.infrastructure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public OpenAPI customOpenAPI() {
        final String oauthSchemeName = "keycloak-oauth2";
        final String bearerSchemeName = "bearer-jwt";
        
        // Keycloak OAuth2 endpoints
        String authorizationUrl = issuerUri + "/protocol/openid-connect/auth";
        String tokenUrl = issuerUri + "/protocol/openid-connect/token";

        return new OpenAPI()
                .info(new Info()
                        .title("MKopp.Dev Backend API")
                        .description("RESTful API for MKopp.Dev personal website and blog platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Michael Kopp")
                                .email("michaeljg.kopp@gmail.com")
                                .url("https://mkopp.dev"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://github.com/MichaelJGKopp/mkopp.dev/blob/main/LICENSE")))
                .servers(List.of(
                        new Server()
                                .url("http://api.localhost")
                                .description("Docker Development Server"),
                        new Server()
                                .url("http://localhost:8200")
                                .description("Local Development Server")
                                ))
                .addSecurityItem(new SecurityRequirement()
                        .addList(bearerSchemeName)
                        .addList(oauthSchemeName))
                .components(new Components()
                        // Bearer token scheme (manual token input)
                        .addSecuritySchemes(bearerSchemeName, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token obtained from Keycloak"))
                        // OAuth2 scheme (automatic flow)
                        .addSecuritySchemes(oauthSchemeName, new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("OAuth2 authentication with Keycloak (Authorization Code Flow)")
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(authorizationUrl)
                                                .tokenUrl(tokenUrl)
                                                .scopes(new Scopes()
                                                        .addString("openid", "OpenID Connect scope")
                                                        .addString("profile", "User profile information")
                                                        .addString("email", "User email address")))
                                                )));
    }
}
