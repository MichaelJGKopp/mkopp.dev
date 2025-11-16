package dev.mkopp.mysite.blog.application.port.out;

import dev.mkopp.mysite.blog.domain.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository {
    Comment save(Comment comment);
    Optional<Comment> findById(UUID id);
    Page<Comment> findTopLevelByBlogPostId(UUID blogPostId, Pageable pageable);
    long countByBlogPostId(UUID blogPostId);
    void deleteById(UUID id);
}
