package dev.mkopp.mysite.blog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

import java.util.UUID;

/**
 * Entity representing a like.
 * Can be used for both BlogPost and Comment likes.
 * Has identity (ID) because we need to track and delete individual likes.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Like {
    
    @Identity
    private UUID id;
    
    private UUID blogPostId;  // nullable - set for blog post likes
    private UUID commentId;    // nullable - set for comment likes
    private UUID userId;
}
