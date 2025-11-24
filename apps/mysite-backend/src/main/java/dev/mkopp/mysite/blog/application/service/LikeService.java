package dev.mkopp.mysite.blog.application.service;

import dev.mkopp.mysite.blog.application.port.out.LikeRepository;
import dev.mkopp.mysite.blog.domain.model.Like;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LikeService {
    
    private final LikeRepository likeRepository;
    
    public void toggleLike(UUID blogPostId, UUID userId) {
        likeRepository.findByBlogPostIdAndUserId(blogPostId, userId)
            .ifPresentOrElse(
                like -> {
                    log.debug("Deleting like with ID: {} for blogPost: {} and user: {}", like.getId(), blogPostId, userId);
                    likeRepository.delete(like);
                },
                () -> {
                    log.debug("Creating like for blogPost: {} and user: {}", blogPostId, userId);
                    Like newLike = Like.builder()
                        .blogPostId(blogPostId)
                        .userId(userId)
                        .build();
                    likeRepository.save(newLike);
                }
            );
    }
    
    @Transactional(readOnly = true)
    public long getLikeCount(UUID blogPostId) {
        long count = likeRepository.countByBlogPostId(blogPostId);
        log.debug("Like count for blogPost {}: {}", blogPostId, count);
        return count;
    }
    
    @Transactional(readOnly = true)
    public boolean isLikedByUser(UUID blogPostId, UUID userId) {
        boolean isLiked = likeRepository.existsByBlogPostIdAndUserId(blogPostId, userId);
        log.debug("isLikedByUser for blogPost: {} and user: {} = {}", blogPostId, userId, isLiked);
        return isLiked;
    }
}
