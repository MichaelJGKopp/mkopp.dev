package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.mapper;

import dev.mkopp.mysite.blog.application.mapper.AuthorMapper;
import dev.mkopp.mysite.blog.domain.model.Author;
import dev.mkopp.mysite.blog.domain.model.BlogPost;
import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.TagJpaRepository;
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
    
    @Autowired
    protected TagJpaRepository tagJpaRepository;
    
    public BlogPost toDomain(BlogPostEntity entity) {
        if (entity == null) {
            return null;
        }
        
        Author author = null;
        if (entity.getAuthorId() != null) {
            author = userApi.getUserById(entity.getAuthorId())
                .map(authorMapper::toAuthor)
                .orElse(null);
        }
        
        Set<String> tags = mapTagsToStrings(entity.getTags());
        
        return BlogPost.builder()
            .id(entity.getId())
            .slug(entity.getSlug())
            .title(entity.getTitle())
            .description(entity.getDescription())
            .content(entity.getContent())
            .author(author)
            .publishedAt(entity.getPublishedAt())
            .thumbnailUrl(entity.getThumbnailUrl())
            .type(entity.getType())
            .externalUrl(entity.getExternalUrl())
            .tags(tags)
            .build();
    }
    
    @Mapping(target = "authorId", source = "domain.author.id")
    @Mapping(target = "tags", ignore = true)
    public abstract BlogPostEntity toEntity(BlogPost domain);
    
    @AfterMapping
    protected void mapTagsToEntity(@MappingTarget BlogPostEntity entity, BlogPost domain) {
        if (domain.getTags() != null && !domain.getTags().isEmpty()) {
            entity.setTags(domain.getTags().stream()
                .map(name -> tagJpaRepository.findByName(name)
                    .orElseGet(() -> tagJpaRepository.save(TagEntity.builder().name(name).build())))
                .collect(Collectors.toSet()));
        }
    }
    
    private Set<String> mapTagsToStrings(Set<TagEntity> tags) {
        if (tags == null) {
            return Set.of();
        }
        return tags.stream()
            .map(TagEntity::getName)
            .collect(Collectors.toSet());
    }
}
