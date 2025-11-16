package dev.mkopp.mysite.blog.application.port.in;

import dev.mkopp.mysite.blog.domain.model.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GetBlogPostUseCase {
    Page<BlogPost> findAll(Pageable pageable);
    BlogPost findBySlug(String slug);
    BlogPost findById(UUID id);
    Page<BlogPost> findByTag(String tagName, Pageable pageable);
}
