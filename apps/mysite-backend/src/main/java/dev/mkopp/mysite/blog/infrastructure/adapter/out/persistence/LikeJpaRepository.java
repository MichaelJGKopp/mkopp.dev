package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence;

import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface LikeJpaRepository extends JpaRepository<LikeEntity, UUID> {
    Optional<LikeEntity> findByBlogPostIdAndUserId(UUID blogPostId, UUID userId);
    boolean existsByBlogPostIdAndUserId(UUID blogPostId, UUID userId);
    long countByBlogPostId(UUID blogPostId);
}
