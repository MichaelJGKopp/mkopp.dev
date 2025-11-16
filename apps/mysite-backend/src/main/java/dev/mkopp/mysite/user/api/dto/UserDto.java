package dev.mkopp.mysite.user.api.dto;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
    UUID id,
    String username,
    String email,
    String firstName,
    String lastName,
    Instant createdAt
) {}
