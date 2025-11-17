package dev.mkopp.mysite.user.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "User response")
public record UserResponse(
    @Schema(description = "User ID") UUID id,
    @Schema(description = "Username") String username,
    @Schema(description = "Email") String email,
    @Schema(description = "First name") String firstName,
    @Schema(description = "Last name") String lastName,
    @Schema(description = "Creation date") Instant createdAt
) {}
