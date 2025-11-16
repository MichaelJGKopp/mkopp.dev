package dev.mkopp.mysite.blog.application.port.out;

import dev.mkopp.mysite.blog.domain.model.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface BlogPostRepository {
    BlogPost save(BlogPost blogPost);
    Optional<BlogPost> findById(UUID id);
    Optional<BlogPost> findBySlug(String slug);
    Page<BlogPost> findAll(Pageable pageable);
    Page<BlogPost> findByTagName(String tagName, Pageable pageable);
    void deleteById(UUID id);
}
