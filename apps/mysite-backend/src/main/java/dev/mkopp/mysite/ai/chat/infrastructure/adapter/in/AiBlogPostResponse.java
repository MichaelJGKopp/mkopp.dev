package dev.mkopp.mysite.ai.chat.infrastructure.adapter.in;

import java.util.List;

public record AiBlogPostResponse(
    String slug,
    String title,
    String description,
    String content,
    List<String> tags
) {}
