package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
class LikeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "blog_post_id", nullable = false)
    private UUID blogPostId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
