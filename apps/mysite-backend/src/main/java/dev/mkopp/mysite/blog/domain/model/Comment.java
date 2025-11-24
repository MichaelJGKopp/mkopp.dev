package dev.mkopp.mysite.blog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    
    @Identity
    private UUID id;
    
    private UUID blogPostId;
    private UUID userId;
    private UUID parentCommentId;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
    
    @Builder.Default
    private List<Comment> replies = new ArrayList<>();
    
    public void updateContent(String content) {
        this.content = content;
    }
    
    public void addReply(Comment reply) {
        this.replies.add(reply);
    }
}
