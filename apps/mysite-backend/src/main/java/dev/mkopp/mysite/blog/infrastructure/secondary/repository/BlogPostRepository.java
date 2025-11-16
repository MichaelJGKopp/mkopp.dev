package dev.mkopp.mysite.blog.infrastructure.secondary.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import dev.mkopp.mysite.blog.domain.model.BlogPost;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {
    Optional<BlogPost> findBySlug(String slug);
    
    @Query("SELECT bp FROM BlogPost bp LEFT JOIN FETCH bp.tags LEFT JOIN FETCH bp.author WHERE bp.slug = :slug")
    Optional<BlogPost> findBySlugWithDetails(String slug);
    
    Page<BlogPost> findAllByOrderByPublishedAtDesc(Pageable pageable);
    
    @Query("SELECT bp FROM BlogPost bp JOIN bp.tags t WHERE t.name = :tagName ORDER BY bp.publishedAt DESC")
    Page<BlogPost> findByTagName(String tagName, Pageable pageable);
}
