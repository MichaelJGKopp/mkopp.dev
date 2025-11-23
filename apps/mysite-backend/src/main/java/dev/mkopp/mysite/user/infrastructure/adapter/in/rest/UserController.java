package dev.mkopp.mysite.user.infrastructure.adapter.in.rest;

import dev.mkopp.mysite.user.application.port.in.GetUserUseCase;
import dev.mkopp.mysite.user.infrastructure.adapter.in.rest.dto.UserResponse;
import dev.mkopp.mysite.user.infrastructure.adapter.in.rest.mapper.UserRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management API")
class UserController {
    
    private final GetUserUseCase getUserUseCase;
    private final UserRestMapper mapper;
    
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "oauth2")
    @Operation(summary = "Get current user")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return getUserUseCase.findById(userId)
            .map(mapper::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        return getUserUseCase.findById(userId)
            .map(mapper::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
