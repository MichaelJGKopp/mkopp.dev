package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence;

import dev.mkopp.mysite.blog.application.port.out.CommentRepository;
import dev.mkopp.mysite.blog.domain.model.Comment;
import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.mapper.CommentEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class CommentRepositoryAdapter implements CommentRepository {
    
    private final CommentJpaRepository jpaRepository;
    private final CommentEntityMapper mapper;
    
    @Override
    public Comment save(Comment comment) {
        var entity = mapper.toEntity(comment);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
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
    public long countByBlogPostId(UUID blogPostId) {
        return jpaRepository.countByBlogPostId(blogPostId);
    }
    
    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
