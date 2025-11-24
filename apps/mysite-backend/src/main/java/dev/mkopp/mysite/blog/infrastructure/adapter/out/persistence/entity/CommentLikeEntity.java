package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity;

import java.util.UUID;

import dev.mkopp.mysite.shared.api.persistence.AbstractAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comment_likes", schema = "blog", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"comment_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentLikeEntity extends AbstractAuditingEntity<UUID> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "comment_id", nullable = false)
    private UUID commentId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
        
    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
