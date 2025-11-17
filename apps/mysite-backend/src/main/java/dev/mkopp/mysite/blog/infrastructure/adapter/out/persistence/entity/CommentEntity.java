package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity;

import dev.mkopp.mysite.shared.api.persistence.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "comments", schema = "blog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentEntity extends AbstractAuditingEntity<UUID> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "blog_post_id", nullable = false)
    private UUID blogPostId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "parent_comment_id")
    private UUID parentCommentId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Override
    public UUID getId() {
        return id;
    }
}
