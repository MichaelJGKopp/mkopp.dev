package dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Comment creation/update request")
public record CommentRequest(
    @Schema(description = "Content") String content,
    @Schema(description = "Parent comment ID") UUID parentCommentId
) {}
