package dev.mkopp.mysite.blog.infrastructure.secondary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.mkopp.mysite.blog.domain.model.Like;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {
    Optional<Like> findByBlogPostIdAndUserId(UUID blogPostId, UUID userId);
    boolean existsByBlogPostIdAndUserId(UUID blogPostId, UUID userId);
    void deleteByBlogPostIdAndUserId(UUID blogPostId, UUID userId);
    long countByBlogPostId(UUID blogPostId);
}
