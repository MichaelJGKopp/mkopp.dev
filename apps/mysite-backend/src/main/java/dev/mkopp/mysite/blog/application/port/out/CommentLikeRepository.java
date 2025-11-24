package dev.mkopp.mysite.blog.application.port.out;

import dev.mkopp.mysite.blog.domain.model.CommentLike;

import java.util.Optional;
import java.util.UUID;

public interface CommentLikeRepository {
    CommentLike save(CommentLike commentLike);
    Optional<CommentLike> findByCommentIdAndUserId(UUID commentId, UUID userId);
    void delete(CommentLike commentLike);
    long countByCommentId(UUID commentId);
    boolean existsByCommentIdAndUserId(UUID commentId, UUID userId);
}
