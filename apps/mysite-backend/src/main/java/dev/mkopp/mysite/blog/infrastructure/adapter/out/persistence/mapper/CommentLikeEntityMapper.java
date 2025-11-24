package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.mapper;

import dev.mkopp.mysite.blog.domain.model.CommentLike;
import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity.CommentLikeEntity;
import org.springframework.stereotype.Component;

@Component
public class CommentLikeEntityMapper {
    
    public CommentLike toDomain(CommentLikeEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return CommentLike.builder()
            .id(entity.getId())
            .commentId(entity.getCommentId())
            .userId(entity.getUserId())
            .build();
    }
    
    public CommentLikeEntity toEntity(CommentLike domain) {
        if (domain == null) {
            return null;
        }
        
        return CommentLikeEntity.builder()
            .id(domain.getId())
            .commentId(domain.getCommentId())
            .userId(domain.getUserId())
            .build();
    }
}
