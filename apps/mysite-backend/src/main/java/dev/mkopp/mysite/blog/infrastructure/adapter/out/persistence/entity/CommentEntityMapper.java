package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity;

import dev.mkopp.mysite.blog.domain.model.Comment;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface CommentEntityMapper {
    
    Comment toDomain(CommentEntity entity);
    
    CommentEntity toEntity(Comment domain);
}
