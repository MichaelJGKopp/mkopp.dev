package dev.mkopp.mysite.blog.application.service;

import dev.mkopp.mysite.blog.application.port.out.CommentLikeRepository;
import dev.mkopp.mysite.blog.domain.model.CommentLike;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentLikeService {
    
    private final CommentLikeRepository commentLikeRepository;
    
    public void toggleCommentLike(UUID commentId, UUID userId) {
        commentLikeRepository.findByCommentIdAndUserId(commentId, userId)
            .ifPresentOrElse(
                like -> {
                    log.debug("Deleting comment like with ID: {} for comment: {} and user: {}", like.getId(), commentId, userId);
                    commentLikeRepository.delete(like);
                },
                () -> {
                    log.debug("Creating comment like for comment: {} and user: {}", commentId, userId);
                    CommentLike newLike = CommentLike.builder()
                        .commentId(commentId)
                        .userId(userId)
                        .build();
                    commentLikeRepository.save(newLike);
                }
            );
    }
    
    @Transactional(readOnly = true)
    public long getCommentLikeCount(UUID commentId) {
        long count = commentLikeRepository.countByCommentId(commentId);
        log.debug("Like count for comment {}: {}", commentId, count);
        return count;
    }
    
    @Transactional(readOnly = true)
    public boolean isCommentLikedByUser(UUID commentId, UUID userId) {
        boolean isLiked = commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
        log.debug("isCommentLikedByUser for comment: {} and user: {} = {}", commentId, userId, isLiked);
        return isLiked;
    }
}
