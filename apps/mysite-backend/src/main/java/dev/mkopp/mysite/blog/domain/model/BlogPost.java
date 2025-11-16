package dev.mkopp.mysite.blog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@AggregateRoot
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPost {
    
    @Identity
    private UUID id;
    
    private String slug;
    private String title;
    private String description;
    private String content;
    private Author author;
    private Instant publishedAt;  // Business-relevant: when was it published?
    private String thumbnailUrl;
    private BlogPostType type;
    private String externalUrl;
    
    @Builder.Default
    private Set<String> tags = new HashSet<>();
    
    
    public void updateContent(String title, String description, String content) {
        this.title = title;
        this.description = description;
        this.content = content;
    }
    
    public void addTag(String tag) {
        this.tags.add(tag);
    }
    
    public void removeTag(String tag) {
        this.tags.remove(tag);
    }
}
