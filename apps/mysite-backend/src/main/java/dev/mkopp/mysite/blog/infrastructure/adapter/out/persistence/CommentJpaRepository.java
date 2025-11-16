package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence;

import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

interface CommentJpaRepository extends JpaRepository<CommentEntity, UUID> {
    
    @Query("SELECT c FROM CommentEntity c WHERE c.blogPostId = :blogPostId AND c.parentCommentId IS NULL ORDER BY c.createdAt DESC")
    Page<CommentEntity> findTopLevelByBlogPostId(UUID blogPostId, Pageable pageable);
    
    long countByBlogPostId(UUID blogPostId);
}
