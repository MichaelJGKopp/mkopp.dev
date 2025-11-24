package dev.mkopp.mysite.blog.application.service;

import dev.mkopp.mysite.blog.application.mapper.CommentEventMapper;
import dev.mkopp.mysite.blog.application.port.out.CommentLikeRepository;
import dev.mkopp.mysite.blog.application.port.out.CommentRepository;
import dev.mkopp.mysite.blog.domain.model.Comment;
import dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto.CommentTreeItem;
import dev.mkopp.mysite.shared.api.exception.ResourceNotFoundException;
import dev.mkopp.mysite.shared.api.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CommentEventMapper eventMapper;
    
    @Transactional(readOnly = true)
    public Page<CommentTreeItem> getTopLevelComments(UUID blogPostId, Pageable pageable) {
        return commentRepository.findTopLevelByBlogPostId(blogPostId, pageable)
            .map(comment -> toCommentTreeItem(comment, 
                commentRepository.countByParentCommentId(comment.getId()),
                commentLikeRepository.countByCommentId(comment.getId())));
    }
    
    @Transactional(readOnly = true)
    public Page<CommentTreeItem> getReplies(UUID parentCommentId, Pageable pageable) {
        return commentRepository.findRepliesByParentCommentId(parentCommentId, pageable)
            .map(comment -> toCommentTreeItem(comment, 
                commentRepository.countByParentCommentId(comment.getId()),
                commentLikeRepository.countByCommentId(comment.getId())));
    }
    
    @Transactional(readOnly = true)
    public long getCommentCount(UUID blogPostId) {
        return commentRepository.countByBlogPostId(blogPostId);
    }
    
    @Transactional(readOnly = true)
    public long getReplyCount(UUID parentCommentId) {
        return commentRepository.countByParentCommentId(parentCommentId);
    }
    
    public Comment createComment(UUID blogPostId, UUID userId, String content, UUID parentCommentId) {
        Comment comment = Comment.builder()
            .blogPostId(blogPostId)
            .userId(userId)
            .parentCommentId(parentCommentId)
            .content(content)
            .build();
        
        Comment savedComment = commentRepository.save(comment);
        
        eventPublisher.publishEvent(eventMapper.toAddedEvent(savedComment));
        
        return savedComment;
    }
    
    public Comment updateComment(UUID commentId, UUID userId, String content) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
        if (!comment.getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to update this comment");
        }
        
        comment.updateContent(content);
        return commentRepository.save(comment);
    }
    
    public void deleteComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
        if (!comment.getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to delete this comment");
        }
        
        commentRepository.deleteById(commentId);
    }
    
    private CommentTreeItem toCommentTreeItem(Comment comment, long replyCount, long likeCount) {
        return new CommentTreeItem(
            comment.getId(),
            comment.getUserId(),
            comment.getContent(),
            comment.getCreatedAt(),
            comment.getUpdatedAt(),
            replyCount,
            likeCount
        );
    }
}
