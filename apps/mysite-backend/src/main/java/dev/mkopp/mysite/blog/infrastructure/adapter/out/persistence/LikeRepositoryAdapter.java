package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence;

import dev.mkopp.mysite.blog.application.port.out.LikeRepository;
import dev.mkopp.mysite.blog.domain.model.Like;
import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.mapper.LikeEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class LikeRepositoryAdapter implements LikeRepository {
    
    private final LikeJpaRepository jpaRepository;
    private final LikeEntityMapper mapper;
    
    @Override
    public Like save(Like like) {
        var entity = mapper.toEntity(like);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Like> findByBlogPostIdAndUserId(UUID blogPostId, UUID userId) {
        return jpaRepository.findByBlogPostIdAndUserId(blogPostId, userId)
            .map(mapper::toDomain);
    }
    
    @Override
    public void delete(Like like) {
        jpaRepository.delete(mapper.toEntity(like));
    }
    
    @Override
    public long countByBlogPostId(UUID blogPostId) {
        return jpaRepository.countByBlogPostId(blogPostId);
    }
    
    @Override
    public boolean existsByBlogPostIdAndUserId(UUID blogPostId, UUID userId) {
        return jpaRepository.existsByBlogPostIdAndUserId(blogPostId, userId);
    }
}
