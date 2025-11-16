package dev.mkopp.mysite.blog.api.event;

import org.jmolecules.event.annotation.DomainEvent;
import org.springframework.modulith.events.Externalized;

import java.time.Instant;
import java.util.UUID;

@DomainEvent
@Externalized("blog.comment.added::#{id()}")
public record CommentAddedEvent(
    UUID id,
    UUID blogPostId,
    UUID userId,
    UUID parentCommentId,
    String content,
    Instant createdAt
) {}
