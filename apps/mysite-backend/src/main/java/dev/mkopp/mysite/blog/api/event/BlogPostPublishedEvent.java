package dev.mkopp.mysite.blog.api.event;

import org.jmolecules.event.annotation.DomainEvent;
import org.springframework.modulith.events.Externalized;

import java.time.Instant;
import java.util.UUID;

@DomainEvent
@Externalized("blog.post.published::#{slug()}")
public record BlogPostPublishedEvent(
    UUID id,
    String slug,
    String title,
    UUID authorId,
    Instant publishedAt
) {}
