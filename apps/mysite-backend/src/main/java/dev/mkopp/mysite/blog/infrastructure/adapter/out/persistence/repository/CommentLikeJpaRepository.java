package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.repository;

import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity.CommentLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommentLikeJpaRepository extends JpaRepository<CommentLikeEntity, UUID> {
    Optional<CommentLikeEntity> findByCommentIdAndUserId(UUID commentId, UUID userId);
    boolean existsByCommentIdAndUserId(UUID commentId, UUID userId);
    long countByCommentId(UUID commentId);
}
