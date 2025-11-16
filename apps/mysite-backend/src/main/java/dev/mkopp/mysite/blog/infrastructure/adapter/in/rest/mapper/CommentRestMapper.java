package dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.mapper;

import dev.mkopp.mysite.blog.domain.model.Comment;
import dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto.CommentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CommentRestMapper {
    
    CommentResponse toResponse(Comment domain);
}
