package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence;

import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity.BlogPostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

interface BlogPostJpaRepository extends JpaRepository<BlogPostEntity, UUID> {
    Optional<BlogPostEntity> findBySlug(String slug);
    
    @Query("SELECT bp FROM BlogPostEntity bp LEFT JOIN FETCH bp.tags WHERE bp.slug = :slug")
    Optional<BlogPostEntity> findBySlugWithTags(String slug);
    
    Page<BlogPostEntity> findAllByOrderByPublishedAtDesc(Pageable pageable);
    
    @Query("SELECT bp FROM BlogPostEntity bp JOIN bp.tags t WHERE t.name = :tagName ORDER BY bp.publishedAt DESC")
    Page<BlogPostEntity> findByTagName(String tagName, Pageable pageable);
}
