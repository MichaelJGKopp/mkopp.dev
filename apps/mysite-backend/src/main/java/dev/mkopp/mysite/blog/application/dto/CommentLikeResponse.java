package dev.mkopp.mysite.blog.application.dto;

public record CommentLikeResponse(
    long count,
    boolean isLiked
) {}
