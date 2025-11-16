package dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto;

import dev.mkopp.mysite.blog.domain.model.BlogPostType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Blog post response")
public record BlogPostResponse(
    @Schema(description = "ID") UUID id,
    @Schema(description = "Slug") String slug,
    @Schema(description = "Title") String title,
    @Schema(description = "Description") String description,
    @Schema(description = "Content") String content,
    @Schema(description = "Author ID") UUID authorId,
    @Schema(description = "Author name") String authorName,
    @Schema(description = "Publication date") Instant publishedAt,
    @Schema(description = "Creation date") Instant createdAt,
    @Schema(description = "Update date") Instant updatedAt,
    @Schema(description = "Thumbnail URL") String thumbnailUrl,
    @Schema(description = "Post type") BlogPostType type,
    @Schema(description = "External URL") String externalUrl,
    @Schema(description = "Tags") Set<String> tags
) {}
