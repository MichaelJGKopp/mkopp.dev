package dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Comment response")
public record CommentResponse(
    @Schema(description = "ID") UUID id,
    @Schema(description = "User ID") UUID userId,
    @Schema(description = "Content") String content,
    @Schema(description = "Parent comment ID") UUID parentCommentId,
    @Schema(description = "Replies") List<CommentResponse> replies,
    @Schema(description = "Creation date") Instant createdAt,
    @Schema(description = "Update date") Instant updatedAt
) {}
