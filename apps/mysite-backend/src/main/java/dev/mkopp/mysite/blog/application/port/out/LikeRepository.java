package dev.mkopp.mysite.blog.application.port.out;

import dev.mkopp.mysite.blog.domain.model.Like;

import java.util.Optional;
import java.util.UUID;

public interface LikeRepository {
    Like save(Like like);
    Optional<Like> findByBlogPostIdAndUserId(UUID blogPostId, UUID userId);
    void delete(Like like);
    long countByBlogPostId(UUID blogPostId);
    boolean existsByBlogPostIdAndUserId(UUID blogPostId, UUID userId);
}
