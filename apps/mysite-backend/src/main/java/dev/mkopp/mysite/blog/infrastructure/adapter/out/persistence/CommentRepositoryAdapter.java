package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence;

import dev.mkopp.mysite.blog.application.port.out.CommentRepository;
import dev.mkopp.mysite.blog.domain.model.Comment;
import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.mapper.CommentEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
class CommentRepositoryAdapter implements CommentRepository {
    
    private final CommentJpaRepository jpaRepository;
    private final CommentEntityMapper mapper;
    
    @Override
    public Comment save(Comment comment) {
        var entity = mapper.toEntity(comment);
        log.debug("Before save - entity createdAt: {}, updatedAt: {}", entity.getCreatedAt(), entity.getUpdatedAt());
        var savedEntity = jpaRepository.save(entity);
        log.debug("After save - entity createdAt: {}, updatedAt: {}", savedEntity.getCreatedAt(), savedEntity.getUpdatedAt());
        var domainComment = mapper.toDomain(savedEntity);
        log.debug("Mapped domain - createdAt: {}, updatedAt: {}", domainComment.getCreatedAt(), domainComment.getUpdatedAt());
        return domainComment;
    }
    
    @Override
    public Optional<Comment> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
    
    @Override
    public Page<Comment> findTopLevelByBlogPostId(UUID blogPostId, Pageable pageable) {
        return jpaRepository.findTopLevelByBlogPostId(blogPostId, pageable)
            .map(mapper::toDomain);
    }
    
    @Override
    public Page<Comment> findRepliesByParentCommentId(UUID parentCommentId, Pageable pageable) {
        return jpaRepository.findRepliesByParentCommentId(parentCommentId, pageable)
            .map(mapper::toDomain);
    }
    
    @Override
    public long countByBlogPostId(UUID blogPostId) {
        return jpaRepository.countByBlogPostId(blogPostId);
    }
    
    @Override
    public long countByParentCommentId(UUID parentCommentId) {
        return jpaRepository.countByParentCommentId(parentCommentId);
    }
    
    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
