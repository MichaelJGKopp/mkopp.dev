package dev.mkopp.mysite.blog.infrastructure.adapter.in.rest;

import dev.mkopp.mysite.blog.application.port.in.*;
import dev.mkopp.mysite.blog.domain.model.BlogPost;
import dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto.BlogPostRequest;
import dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto.BlogPostResponse;
import dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.mapper.BlogPostRestMapper;
import dev.mkopp.mysite.user.application.port.in.FindOrCreateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping("/v1/blog")
@RequiredArgsConstructor
@Tag(name = "Blog", description = "Blog post management API")
public class BlogPostController {
    
    private final GetBlogPostUseCase getBlogPostUseCase;
    private final CreateBlogPostUseCase createBlogPostUseCase;
    private final UpdateBlogPostUseCase updateBlogPostUseCase;
    private final DeleteBlogPostUseCase deleteBlogPostUseCase;
    private final FindOrCreateUserUseCase findOrCreateUserUseCase;
    private final BlogPostRestMapper mapper;
    
    @GetMapping
    @Operation(summary = "Get all blog posts")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved blog posts")
    public ResponseEntity<Page<BlogPostResponse>> getAllPosts(Pageable pageable) {
        Page<BlogPost> posts = getBlogPostUseCase.findAll(pageable);
        return ResponseEntity.ok(posts.map(mapper::toResponse));
    }
    
    @GetMapping("/{slug}")
    @Operation(summary = "Get blog post by slug")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved blog post")
    @ApiResponse(responseCode = "404", description = "Blog post not found")
    public ResponseEntity<BlogPostResponse> getPostBySlug(@PathVariable String slug) {
        BlogPost post = getBlogPostUseCase.findBySlug(slug);
        return ResponseEntity.ok(mapper.toResponse(post));
    }
    
    @GetMapping("/tag/{tagName}")
    @Operation(summary = "Get blog posts by tag")
    public ResponseEntity<Page<BlogPostResponse>> getPostsByTag(@PathVariable String tagName, Pageable pageable) {
        Page<BlogPost> posts = getBlogPostUseCase.findByTag(tagName, pageable);
        return ResponseEntity.ok(posts.map(mapper::toResponse));
    }
    
    @PostMapping
    @SecurityRequirement(name = "oauth2")
    @Operation(summary = "Create blog post")
    public ResponseEntity<BlogPostResponse> createPost(
            @RequestBody BlogPostRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractAndEnsureUser(jwt);
        BlogPost post = mapper.toDomain(request);
        BlogPost created = createBlogPostUseCase.execute(post, request.tags(), userId);
        return ResponseEntity.ok(mapper.toResponse(created));
    }
    
    @PutMapping("/{id}")
    @SecurityRequirement(name = "oauth2")
    @Operation(summary = "Update blog post")
    public ResponseEntity<BlogPostResponse> updatePost(
            @PathVariable UUID id,
            @RequestBody BlogPostRequest request) {
        BlogPost post = mapper.toDomain(request);
        BlogPost updated = updateBlogPostUseCase.execute(id, post, request.tags());
        return ResponseEntity.ok(mapper.toResponse(updated));
    }
    
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "oauth2")
    @Operation(summary = "Delete blog post")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        deleteBlogPostUseCase.execute(id);
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
