package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.mapper;

import dev.mkopp.mysite.blog.application.mapper.AuthorMapper;
import dev.mkopp.mysite.blog.domain.model.Author;
import dev.mkopp.mysite.blog.domain.model.BlogPost;
import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity.BlogPostEntity;
import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity.TagEntity;
import dev.mkopp.mysite.user.api.UserApi;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuthorMapper.class}
)
public abstract class BlogPostEntityMapper {
    
    @Autowired
    protected UserApi userApi;
    
    @Autowired
    protected AuthorMapper authorMapper;
    
    @Mapping(target = "author", source = "entity", qualifiedByName = "mapAuthor")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "mapTagsToStrings")
    public abstract BlogPost toDomain(BlogPostEntity entity);
    
    @Mapping(target = "authorId", source = "domain.author.id")
    @Mapping(target = "tags", ignore = true)
    public abstract BlogPostEntity toEntity(BlogPost domain);
    
    @AfterMapping
    protected void mapTagsToEntity(@MappingTarget BlogPostEntity entity, BlogPost domain) {
        if (domain.getTags() != null) {
            entity.setTags(domain.getTags().stream()
                .map(name -> TagEntity.builder().name(name).build())
                .collect(Collectors.toSet()));
        }
    }
    
    @Named("mapAuthor")
    protected Author mapAuthor(BlogPostEntity entity) {
        if (entity.getAuthorId() == null) {
            return null;
        }
        
        return userApi.getUserById(entity.getAuthorId())
            .map(authorMapper::toAuthor)
            .orElse(null);
    }
    
    @Named("mapTagsToStrings")
    protected Set<String> mapTagsToStrings(Set<TagEntity> tags) {
        if (tags == null) {
            return Set.of();
        }
        return tags.stream()
            .map(TagEntity::getName)
            .collect(Collectors.toSet());
    }
}
