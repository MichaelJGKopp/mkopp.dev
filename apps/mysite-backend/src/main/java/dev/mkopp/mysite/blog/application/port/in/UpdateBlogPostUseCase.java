package dev.mkopp.mysite.blog.application.port.in;

import dev.mkopp.mysite.blog.domain.model.BlogPost;

import java.util.Set;
import java.util.UUID;

public interface UpdateBlogPostUseCase {
    BlogPost execute(UUID id, BlogPost blogPost, Set<String> tags);
}
