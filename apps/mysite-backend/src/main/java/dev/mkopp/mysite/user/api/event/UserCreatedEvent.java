package dev.mkopp.mysite.user.api.event;

import org.jmolecules.event.annotation.DomainEvent;
import org.springframework.modulith.events.Externalized;

import java.time.Instant;
import java.util.UUID;

@DomainEvent
@Externalized("user.created::#{id()}")
public record UserCreatedEvent(
    UUID id,
    String username,
    String email,
    String firstName,
    String lastName,
    Instant createdAt
) {}
