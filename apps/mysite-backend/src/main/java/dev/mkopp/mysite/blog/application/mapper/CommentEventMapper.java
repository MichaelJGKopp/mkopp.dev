package dev.mkopp.mysite.blog.application.mapper;

import dev.mkopp.mysite.blog.api.event.CommentAddedEvent;
import dev.mkopp.mysite.blog.domain.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CommentEventMapper {
    
    CommentAddedEvent toAddedEvent(Comment comment);
}
