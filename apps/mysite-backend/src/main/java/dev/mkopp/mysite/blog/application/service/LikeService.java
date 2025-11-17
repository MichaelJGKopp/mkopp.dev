package dev.mkopp.mysite.blog.application.service;

import dev.mkopp.mysite.blog.application.port.out.LikeRepository;
import dev.mkopp.mysite.blog.domain.model.Like;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {
    
    private final LikeRepository likeRepository;
    
    public void toggleLike(UUID blogPostId, UUID userId) {
        likeRepository.findByBlogPostIdAndUserId(blogPostId, userId)
            .ifPresentOrElse(
                likeRepository::delete,
                () -> {
                    Like newLike = Like.builder()
                        .id(UUID.randomUUID())
                        .blogPostId(blogPostId)
                        .userId(userId)
                        .build();
                    likeRepository.save(newLike);
                }
            );
    }
    
    @Transactional(readOnly = true)
    public long getLikeCount(UUID blogPostId) {
        return likeRepository.countByBlogPostId(blogPostId);
    }
    
    @Transactional(readOnly = true)
    public boolean isLikedByUser(UUID blogPostId, UUID userId) {
        return likeRepository.existsByBlogPostIdAndUserId(blogPostId, userId);
    }
}
