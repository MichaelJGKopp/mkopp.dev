package dev.mkopp.mysite.blog.infrastructure.adapter.in.rest;

import dev.mkopp.mysite.blog.application.service.CommentService;
import dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto.CommentRequest;
import dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto.CommentResponse;
import dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.mapper.CommentRestMapper;
import dev.mkopp.mysite.user.application.port.in.FindOrCreateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/blog/{blogPostId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Blog comment management API")
class CommentController {
    
    private final CommentService commentService;
    private final FindOrCreateUserUseCase findOrCreateUserUseCase;
    private final CommentRestMapper mapper;
    
    @GetMapping
    @Operation(summary = "Get comments for blog post")
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable UUID blogPostId,
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getTopLevelCommentsByPostId(blogPostId, pageable)
            .map(mapper::toResponse));
    }
    
    @PostMapping
    @SecurityRequirement(name = "oauth2")
    @Operation(summary = "Create comment")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable UUID blogPostId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractAndEnsureUser(jwt);
        var comment = commentService.createComment(blogPostId, userId, request.content(), request.parentCommentId());
        return ResponseEntity.ok(mapper.toResponse(comment));
    }
    
    @PutMapping("/{commentId}")
    @SecurityRequirement(name = "oauth2")
    @Operation(summary = "Update comment")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID blogPostId,
            @PathVariable UUID commentId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractAndEnsureUser(jwt);
        var comment = commentService.updateComment(commentId, userId, request.content());
        return ResponseEntity.ok(mapper.toResponse(comment));
    }
    
    @DeleteMapping("/{commentId}")
    @SecurityRequirement(name = "oauth2")
    @Operation(summary = "Delete comment")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID blogPostId,
            @PathVariable UUID commentId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractAndEnsureUser(jwt);
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
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
