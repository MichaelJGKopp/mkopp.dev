package dev.mkopp.mysite.blog.infrastructure.adapter.in.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentTreeItem(
    UUID id,
    UUID userId,
    String content,
    Instant createdAt,
    Instant updatedAt,
    long replyCount,
    long likeCount
) {}
