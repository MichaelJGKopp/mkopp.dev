package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence;

import dev.mkopp.mysite.blog.application.port.out.BlogPostRepository;
import dev.mkopp.mysite.blog.domain.model.BlogPost;
import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.mapper.BlogPostEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class BlogPostRepositoryAdapter implements BlogPostRepository {
    
    private final BlogPostJpaRepository jpaRepository;
    private final BlogPostEntityMapper mapper;
    
    @Override
    public BlogPost save(BlogPost blogPost) {
        var entity = mapper.toEntity(blogPost);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<BlogPost> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
    
    @Override
    public Optional<BlogPost> findBySlug(String slug) {
        return jpaRepository.findBySlugWithTags(slug).map(mapper::toDomain);
    }
    
    @Override
    public Page<BlogPost> findAll(Pageable pageable) {
        return jpaRepository.findAllByOrderByPublishedAtDesc(pageable)
            .map(mapper::toDomain);
    }
    
    @Override
    public Page<BlogPost> findByTagName(String tagName, Pageable pageable) {
        return jpaRepository.findByTagName(tagName, pageable)
            .map(mapper::toDomain);
    }
    
    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
