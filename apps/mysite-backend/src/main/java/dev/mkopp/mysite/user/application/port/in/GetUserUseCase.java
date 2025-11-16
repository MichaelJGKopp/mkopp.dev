package dev.mkopp.mysite.user.application.port.in;

import dev.mkopp.mysite.user.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface GetUserUseCase {
    Optional<User> findById(UUID userId);
    Optional<User> findByUsername(String username);
}
