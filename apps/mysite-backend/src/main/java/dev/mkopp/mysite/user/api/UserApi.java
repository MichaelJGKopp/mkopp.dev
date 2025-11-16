package dev.mkopp.mysite.user.api;

import dev.mkopp.mysite.user.api.dto.UserDto;

import java.util.Optional;
import java.util.UUID;

public interface UserApi {
    Optional<UserDto> getUserById(UUID userId);
    Optional<UserDto> getUserByUsername(String username);
}
