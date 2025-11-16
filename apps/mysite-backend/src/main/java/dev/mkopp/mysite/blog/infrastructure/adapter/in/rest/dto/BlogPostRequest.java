package dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto;

import dev.mkopp.mysite.blog.domain.model.BlogPostType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;

@Schema(description = "Blog post creation/update request")
public record BlogPostRequest(
    @Schema(description = "Unique slug", example = "my-first-post") String slug,
    @Schema(description = "Title") String title,
    @Schema(description = "Description") String description,
    @Schema(description = "Content") String content,
    @Schema(description = "Publication date") Instant publishedAt,
    @Schema(description = "Thumbnail URL") String thumbnailUrl,
    @Schema(description = "Post type") BlogPostType type,
    @Schema(description = "External URL") String externalUrl,
    @Schema(description = "Tags") Set<String> tags
) {}
