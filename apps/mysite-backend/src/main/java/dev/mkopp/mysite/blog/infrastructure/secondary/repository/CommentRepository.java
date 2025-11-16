package dev.mkopp.mysite.blog.infrastructure.secondary.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import dev.mkopp.mysite.blog.domain.model.Comment;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    
    @Query("SELECT c FROM Comment c WHERE c.blogPost.id = :blogPostId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findTopLevelCommentsByBlogPostId(UUID blogPostId, Pageable pageable);
    
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user WHERE c.blogPost.id = :blogPostId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findTopLevelCommentsByBlogPostIdWithUser(UUID blogPostId);
    
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.replies WHERE c.parentComment.id = :parentCommentId ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentCommentId(UUID parentCommentId);
    
    long countByBlogPostId(UUID blogPostId);
    
    boolean existsByIdAndUserId(UUID commentId, UUID userId);
}
