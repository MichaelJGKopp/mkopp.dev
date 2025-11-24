package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.mapper;

import dev.mkopp.mysite.blog.domain.model.Comment;
import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity.CommentEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CommentEntityMapper {
    
    public Comment toDomain(CommentEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Comment.builder()
            .id(entity.getId())
            .blogPostId(entity.getBlogPostId())
            .userId(entity.getUserId())
            .parentCommentId(entity.getParentCommentId())
            .content(entity.getContent())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .replies(new ArrayList<>())
            .build();
    }
    
    public CommentEntity toEntity(Comment domain) {
        if (domain == null) {
            return null;
        }
        
        return CommentEntity.builder()
            .id(domain.getId())
            .blogPostId(domain.getBlogPostId())
            .userId(domain.getUserId())
            .parentCommentId(domain.getParentCommentId())
            .content(domain.getContent())
            .build();
    }
}
