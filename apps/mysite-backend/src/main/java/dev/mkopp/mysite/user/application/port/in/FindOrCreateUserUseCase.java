package dev.mkopp.mysite.user.application.port.in;

import dev.mkopp.mysite.user.domain.model.User;

import java.util.UUID;

public interface FindOrCreateUserUseCase {
    User execute(UUID keycloakId, String username, String email, String firstName, String lastName);
}
