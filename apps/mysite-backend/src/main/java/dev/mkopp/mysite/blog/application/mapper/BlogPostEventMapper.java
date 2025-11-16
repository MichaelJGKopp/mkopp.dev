package dev.mkopp.mysite.blog.application.mapper;

import dev.mkopp.mysite.blog.api.event.BlogPostPublishedEvent;
import dev.mkopp.mysite.blog.domain.model.BlogPost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BlogPostEventMapper {
    
    @Mapping(target = "authorId", source = "author.id")
    BlogPostPublishedEvent toPublishedEvent(BlogPost blogPost);
}
