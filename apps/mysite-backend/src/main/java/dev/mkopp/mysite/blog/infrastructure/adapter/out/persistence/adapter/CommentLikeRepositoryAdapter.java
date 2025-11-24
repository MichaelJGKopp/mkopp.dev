package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.adapter;

import dev.mkopp.mysite.blog.application.port.out.CommentLikeRepository;
import dev.mkopp.mysite.blog.domain.model.CommentLike;
import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.mapper.CommentLikeEntityMapper;
import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.repository.CommentLikeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CommentLikeRepositoryAdapter implements CommentLikeRepository {
    
    private final CommentLikeJpaRepository jpaRepository;
    private final CommentLikeEntityMapper mapper;
    
    @Override
    public CommentLike save(CommentLike commentLike) {
        var entity = mapper.toEntity(commentLike);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<CommentLike> findByCommentIdAndUserId(UUID commentId, UUID userId) {
        return jpaRepository.findByCommentIdAndUserId(commentId, userId)
            .map(mapper::toDomain);
    }
    
    @Override
    public void delete(CommentLike commentLike) {
        jpaRepository.deleteById(commentLike.getId());
    }
    
    @Override
    public long countByCommentId(UUID commentId) {
        return jpaRepository.countByCommentId(commentId);
    }
    
    @Override
    public boolean existsByCommentIdAndUserId(UUID commentId, UUID userId) {
        return jpaRepository.existsByCommentIdAndUserId(commentId, userId);
    }
}
