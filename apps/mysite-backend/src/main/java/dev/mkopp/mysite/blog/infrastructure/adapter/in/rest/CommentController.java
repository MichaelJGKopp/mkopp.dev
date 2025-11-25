package dev.mkopp.mysite.blog.infrastructure.adapter.in.rest;

import dev.mkopp.mysite.blog.application.dto.CommentLikeResponse;
import dev.mkopp.mysite.blog.application.service.CommentLikeService;
import dev.mkopp.mysite.blog.application.service.CommentService;
import dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto.CommentRequest;
import dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto.CommentTreeItem;
import dev.mkopp.mysite.user.application.port.in.FindOrCreateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Blog comment management API")
class CommentController {
    
    private final CommentService commentService;
    private final CommentLikeService commentLikeService;
    private final FindOrCreateUserUseCase findOrCreateUserUseCase;
    
    @GetMapping(value = "/v1/blog/{blogPostId}/comments/count", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get comment count for blog post")
    public ResponseEntity<Long> getCommentCount(@PathVariable UUID blogPostId) {
        return ResponseEntity.ok(commentService.getCommentCount(blogPostId));
    }
    
    @GetMapping(value = "/v1/blog/{blogPostId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get top-level comments for blog post")
    public ResponseEntity<Page<CommentTreeItem>> getTopLevelComments(
            @PathVariable UUID blogPostId,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(commentService.getTopLevelComments(blogPostId, pageable));
    }
    
    @GetMapping(value = "/v1/comments/{commentId}/replies", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get replies for a comment")
    public ResponseEntity<Page<CommentTreeItem>> getReplies(
            @PathVariable UUID commentId,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(commentService.getReplies(commentId, pageable));
    }
    
    @PostMapping(value = "/v1/blog/{blogPostId}/comments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "oauth2")
    @Operation(summary = "Create comment")
    public ResponseEntity<CommentTreeItem> createComment(
            @PathVariable UUID blogPostId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractAndEnsureUser(jwt);
        var comment = commentService.createComment(blogPostId, userId, request.content(), request.parentCommentId());
        long replyCount = commentService.getReplyCount(comment.getId());
        long likeCount = commentLikeService.getCommentLikeCount(comment.getId());
        return ResponseEntity.ok(new CommentTreeItem(
            comment.getId(),
            comment.getUserId(),
            comment.getContent(),
            comment.getCreatedAt(),
            comment.getUpdatedAt(),
            replyCount,
            likeCount
        ));
    }
    
    @PatchMapping(value = "/v1/comments/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "oauth2")
    @Operation(summary = "Update comment (PATCH)")
    public ResponseEntity<CommentTreeItem> updateComment(
            @PathVariable UUID commentId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractAndEnsureUser(jwt);
        var comment = commentService.updateComment(commentId, userId, request.content());
        long replyCount = commentService.getReplyCount(comment.getId());
        long likeCount = commentLikeService.getCommentLikeCount(comment.getId());
        return ResponseEntity.ok(new CommentTreeItem(
            comment.getId(),
            comment.getUserId(),
            comment.getContent(),
            comment.getCreatedAt(),
            comment.getUpdatedAt(),
            replyCount,
            likeCount
        ));
    }
    
    @DeleteMapping("/v1/comments/{commentId}")
    @SecurityRequirement(name = "oauth2")
    @Operation(summary = "Delete comment")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractAndEnsureUser(jwt);
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
    
    // Comment Like endpoints
    
    @PostMapping(value = "/v1/comments/{commentId}/like", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "oauth2")
    @Operation(summary = "Toggle like on comment")
    public ResponseEntity<CommentLikeResponse> toggleCommentLike(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractAndEnsureUser(jwt);
        commentLikeService.toggleCommentLike(commentId, userId);
        
        return ResponseEntity.ok(new CommentLikeResponse(
            commentLikeService.getCommentLikeCount(commentId),
            commentLikeService.isCommentLikedByUser(commentId, userId)
        ));
    }
    
    @GetMapping(value = "/v1/comments/{commentId}/like", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get comment like count and user like status")
    public ResponseEntity<CommentLikeResponse> getCommentLikeInfo(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal(errorOnInvalidType = false) Jwt jwt) {
        long count = commentLikeService.getCommentLikeCount(commentId);
        boolean isLiked = false;
        
        if (jwt != null) {
            UUID userId = UUID.fromString(jwt.getSubject());
            isLiked = commentLikeService.isCommentLikedByUser(commentId, userId);
        }
        
        return ResponseEntity.ok(new CommentLikeResponse(count, isLiked));
    }
    
    private UUID extractAndEnsureUser(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");
        
        findOrCreateUserUseCase.execute(userId, username, email, firstName, lastName);
        return userId;
    }
}
