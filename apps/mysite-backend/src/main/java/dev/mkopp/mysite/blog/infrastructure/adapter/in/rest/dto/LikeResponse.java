package dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Like information")
public record LikeResponse(
    @Schema(description = "Like count") Long likeCount,
    @Schema(description = "Is liked by user") Boolean isLiked
) {}
