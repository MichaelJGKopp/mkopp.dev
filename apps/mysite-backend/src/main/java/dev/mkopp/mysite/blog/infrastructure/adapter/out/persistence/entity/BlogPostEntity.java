package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity;

import dev.mkopp.mysite.blog.domain.model.BlogPostType;
import dev.mkopp.mysite.shared.api.persistence.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "blog_posts", schema = "blog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class BlogPostEntity extends AbstractAuditingEntity<UUID> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String slug;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "author_id", nullable = false)
    private UUID authorId;
    
    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;
    
    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private BlogPostType type = BlogPostType.BLOG;
    
    @Column(name = "external_url", length = 1000)
    private String externalUrl;
    
    @ManyToMany
    @JoinTable(
        name = "blog_post_tags",
        schema = "blog",
        joinColumns = @JoinColumn(name = "blog_post_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<TagEntity> tags = new HashSet<>();
    
    @Override
    public UUID getId() {
        return id;
    }
}
