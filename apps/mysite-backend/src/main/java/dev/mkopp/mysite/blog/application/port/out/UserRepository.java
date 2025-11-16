package dev.mkopp.mysite.blog.application.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * Port to access User module from Blog module
 */
public interface UserRepository {
    Optional<UserInfo> findById(UUID userId);
    
    record UserInfo(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName
    ) {}
}
