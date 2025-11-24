package dev.mkopp.mysite.blog.domain.model;

import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.util.UUID;

/**
 * Represents a like on a comment.
 */
@Getter
@Builder
@AggregateRoot
public class CommentLike {
    
    @Identity
    private UUID id;
    private UUID commentId;
    private UUID userId;
}
