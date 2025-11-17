package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import dev.mkopp.mysite.shared.api.persistence.AbstractAuditingEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blog_likes", schema = "blog", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"blog_post_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeEntity extends AbstractAuditingEntity<UUID> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "blog_post_id", nullable = false)
    private UUID blogPostId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
        
    @Override
    public UUID getId() {
        return id;
    }
}
