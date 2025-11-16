package dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.mapper;

import dev.mkopp.mysite.blog.domain.model.Author;
import dev.mkopp.mysite.blog.domain.model.BlogPost;
import dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto.BlogPostRequest;
import dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto.BlogPostResponse;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BlogPostRestMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BlogPost toDomain(BlogPostRequest request);
    
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author", qualifiedByName = "mapAuthorName")
    BlogPostResponse toResponse(BlogPost domain);
    
    @Named("mapAuthorName")
    default String mapAuthorName(Author author) {
        if (author == null) return null;
        return String.format("%s %s", author.getFirstName(), author.getLastName()).trim();
    }
}
