package dev.mkopp.mysite.blog.infrastructure.adapter.in.rest;

import dev.mkopp.mysite.blog.application.service.LikeService;
import dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto.LikeResponse;
import dev.mkopp.mysite.user.application.port.in.FindOrCreateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/blog/{blogPostId}/like")
@RequiredArgsConstructor
@Tag(name = "Likes", description = "Blog like management API")
class LikeController {
    
    private final LikeService likeService;
    private final FindOrCreateUserUseCase findOrCreateUserUseCase;
    
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "oauth2")
    @Operation(summary = "Toggle like")
    public ResponseEntity<LikeResponse> toggleLike(
            @PathVariable UUID blogPostId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractAndEnsureUser(jwt);
        likeService.toggleLike(blogPostId, userId);
        return ResponseEntity.ok(new LikeResponse(
            likeService.getLikeCount(blogPostId),
            likeService.isLikedByUser(blogPostId, userId)
        ));
    }
    
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get like info")
    public ResponseEntity<LikeResponse> getLikeInfo(
            @PathVariable UUID blogPostId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        UUID userId = jwt != null ? extractAndEnsureUser(jwt) : null;
        return ResponseEntity.ok(new LikeResponse(
            likeService.getLikeCount(blogPostId),
            userId != null && likeService.isLikedByUser(blogPostId, userId)
        ));
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
