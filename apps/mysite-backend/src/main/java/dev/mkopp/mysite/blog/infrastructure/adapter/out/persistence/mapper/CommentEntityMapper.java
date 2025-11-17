package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.mapper;

import dev.mkopp.mysite.blog.domain.model.Comment;
import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity.CommentEntity;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CommentEntityMapper {
    
    Comment toDomain(CommentEntity entity);
    
    CommentEntity toEntity(Comment domain);
}
