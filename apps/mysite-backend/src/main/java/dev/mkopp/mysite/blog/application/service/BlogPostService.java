package dev.mkopp.mysite.blog.application.service;

import dev.mkopp.mysite.blog.application.mapper.AuthorMapper;
import dev.mkopp.mysite.blog.application.mapper.BlogPostEventMapper;
import dev.mkopp.mysite.blog.api.event.BlogPostPublishedEvent;
import dev.mkopp.mysite.blog.application.port.in.*;
import dev.mkopp.mysite.blog.application.port.out.BlogPostRepository;
import dev.mkopp.mysite.blog.application.port.out.TagRepository;
import dev.mkopp.mysite.blog.domain.model.Author;
import dev.mkopp.mysite.blog.domain.model.BlogPost;
import dev.mkopp.mysite.shared.api.exception.ResourceNotFoundException;
import dev.mkopp.mysite.user.api.UserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
class BlogPostService implements CreateBlogPostUseCase, GetBlogPostUseCase, UpdateBlogPostUseCase, DeleteBlogPostUseCase {
    
    private final BlogPostRepository blogPostRepository;
    private final TagRepository tagRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserApi userApi;
    private final BlogPostEventMapper eventMapper;
    private final AuthorMapper authorMapper;
    
    @Override
    public BlogPost execute(BlogPost blogPost, Set<String> tags, UUID authorId) {
        Author author = userApi.getUserById(authorId)
            .map(authorMapper::toAuthor)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));
        
        blogPost = BlogPost.builder()
            .slug(blogPost.getSlug())
            .title(blogPost.getTitle())
            .description(blogPost.getDescription())
            .content(blogPost.getContent())
            .author(author)
            .publishedAt(blogPost.getPublishedAt() != null ? blogPost.getPublishedAt() : Instant.now())
            .thumbnailUrl(blogPost.getThumbnailUrl())
            .type(blogPost.getType())
            .externalUrl(blogPost.getExternalUrl())
            .tags(tagRepository.findOrCreateTags(tags))
            .build();
        
        BlogPost savedPost = blogPostRepository.save(blogPost);
        
        eventPublisher.publishEvent(eventMapper.toPublishedEvent(savedPost));
        
        log.info("Created blog post: {}", savedPost.getSlug());
        return savedPost;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BlogPost> findAll(Pageable pageable) {
        return blogPostRepository.findAll(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BlogPost findBySlug(String slug) {
        return blogPostRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("BlogPost", "slug", slug));
    }
    
    @Override
    @Transactional(readOnly = true)
    public BlogPost findById(UUID id) {
        return blogPostRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("BlogPost", "id", id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BlogPost> findByTag(String tagName, Pageable pageable) {
        return blogPostRepository.findByTagName(tagName, pageable);
    }
    
    @Override
    public BlogPost execute(UUID id, BlogPost updatedPost, Set<String> tags) {
        BlogPost existingPost = findById(id);
        
        existingPost.updateContent(
            updatedPost.getTitle(),
            updatedPost.getDescription(),
            updatedPost.getContent()
        );
        
        if (tags != null) {
            Set<String> processedTags = tagRepository.findOrCreateTags(tags);
            existingPost.getTags().clear();
            processedTags.forEach(existingPost::addTag);
        }
        
        return blogPostRepository.save(existingPost);
    }
    
    @Override
    public void execute(UUID id) {
        if (!blogPostRepository.findById(id).isPresent()) {
            throw new ResourceNotFoundException("BlogPost", "id", id);
        }
        blogPostRepository.deleteById(id);
    }
}
