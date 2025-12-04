---
title: Building a Database-Driven Blog with PostgreSQL, Flyway, and Angular SSR
slug: blog-system-postgresql-flyway-ssr
author: Michael Kopp
publishedAt: 2025-12-04T11:00:00Z
tags:
  - blog
  - postgresql
  - flyway
  - ssr
  - angular
  - markdown
  - database
description: Building a full-featured blog system with PostgreSQL storage, Flyway migrations, Angular SSR, nested comments, and like functionality.
type: technical
status: draft
---

---

## Introduction

When I built the blog system for my portfolio, I wanted to achieve three key goals:

1. **SEO-Friendly:** Blog posts should be crawlable by search engines
2. **Developer-Friendly:** Writing posts in Markdown with syntax highlighting
3. **Dynamic:** Support comments, likes, and real-time updates without rebuilding

This post explains how I combined PostgreSQL storage, Flyway migrations, Angular SSR, and Markdown rendering to build a fully-featured blog system.

## Architecture Overview

The blog system consists of several layers:

```
┌────────────────────────────────────────────────┐
│  Markdown Source Files (.md)                   │
│  - Blog post content in Markdown               │
│  - Metadata (title, date, tags)                │
└────────────┬───────────────────────────────────┘
             │ Flyway Migration Script
┌────────────▼───────────────────────────────────┐
│  PostgreSQL Database                           │
│  - blog.posts (id, title, content, author...)  │
│  - blog.comments (nested, threaded)            │
│  - blog.likes (posts + comments)               │
└────────────┬───────────────────────────────────┘
             │ Spring Data JPA
┌────────────▼───────────────────────────────────┐
│  BlogController (REST API)                     │
│  - GET /api/blog/posts                         │
│  - POST /api/blog/posts                        │
│  - POST /api/blog/comments                     │
│  - POST /api/blog/likes                        │
└────────────┬───────────────────────────────────┘
             │ OpenAPI Client
┌────────────▼───────────────────────────────────┐
│  Angular SSR Frontend                          │
│  - Blog post resolver (SSR retrieval)          │
│  - ngx-markdown rendering                      │
│  - highlight.js syntax highlighting            │
│  - Comment/like components                     │
└────────────────────────────────────────────────┘
```

## Why Database-Driven?

I evaluated several approaches for blog content management:

### 1. Static Site Generation (SSG)
**Pros:**
- Fast performance (pre-rendered HTML)
- No database required
- Simple deployment

**Cons:**
- Rebuild required for every post/comment
- No dynamic content (comments, likes)
- Scalability issues with large content

### 2. Client-Side Only
**Pros:**
- Simple implementation
- No SSR complexity

**Cons:**
- Poor SEO (content not crawlable)
- Slow initial render
- No progressive enhancement

### 3. Database + SSR (Chosen Approach)
**Pros:**
- Excellent SEO (server-rendered HTML)
- Dynamic content support (comments, likes)
- Searchable and filterable
- Fast perceived performance

**Cons:**
- Database load (mitigated with caching)
- SSR complexity

For the complete decision rationale, see [ADR-012: SSR Blog Optimization with Database](../adr/0012-ssr-blog-optimization-with-database.md).

## Database Schema

The PostgreSQL schema supports blog posts, comments, and likes:

```sql
-- Blog posts table
CREATE TABLE IF NOT EXISTS blog.posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    content TEXT NOT NULL,
    excerpt VARCHAR(500),
    author VARCHAR(100) NOT NULL,
    published_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PUBLISHED',
    tags TEXT[]
);

-- Comments table (nested/threaded)
CREATE TABLE IF NOT EXISTS blog.comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES blog.posts(id) ON DELETE CASCADE,
    parent_id BIGINT REFERENCES blog.comments(id) ON DELETE CASCADE,
    author VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_post FOREIGN KEY (post_id) REFERENCES blog.posts(id),
    CONSTRAINT fk_parent FOREIGN KEY (parent_id) REFERENCES blog.comments(id)
);

-- Likes table (for posts and comments)
CREATE TABLE IF NOT EXISTS blog.likes (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    post_id BIGINT REFERENCES blog.posts(id) ON DELETE CASCADE,
    comment_id BIGINT REFERENCES blog.comments(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT unique_user_post_like UNIQUE (user_id, post_id),
    CONSTRAINT unique_user_comment_like UNIQUE (user_id, comment_id),
    CONSTRAINT check_like_target CHECK (
        (post_id IS NOT NULL AND comment_id IS NULL) OR
        (post_id IS NULL AND comment_id IS NOT NULL)
    )
);

-- Indexes for performance
CREATE INDEX idx_posts_slug ON blog.posts(slug);
CREATE INDEX idx_posts_published_at ON blog.posts(published_at DESC);
CREATE INDEX idx_comments_post_id ON blog.comments(post_id);
CREATE INDEX idx_comments_parent_id ON blog.comments(parent_id);
CREATE INDEX idx_likes_post_id ON blog.likes(post_id);
CREATE INDEX idx_likes_comment_id ON blog.likes(comment_id);
```

**Key Features:**
- **Nested Comments:** `parent_id` foreign key enables threaded replies
- **Likes Constraint:** Prevents duplicate likes (unique constraint per user/post or user/comment)
- **Performance Indexes:** Optimizes common queries (posts by date, comments by post, etc.)
- **Cascading Deletes:** Automatically removes comments/likes when posts are deleted

## Flyway Migrations

Flyway manages database schema evolution:

### Migration 1: Initialize Schema
**File:** `V1_1_2__init_blog.sql`

```sql
-- Create blog schema
CREATE SCHEMA IF NOT EXISTS blog;

-- Create posts table
CREATE TABLE IF NOT EXISTS blog.posts (
    -- (schema from above)
);

-- Create comments table
CREATE TABLE IF NOT EXISTS blog.comments (
    -- (schema from above)
);

-- Create likes table
CREATE TABLE IF NOT EXISTS blog.likes (
    -- (schema from above)
);
```

### Migration 2: Seed Blog Posts
**File:** `V1_1_4__seed_blog_posts.sql`

This migration inserts blog posts from Markdown source files:

```sql
-- Insert blog post from Markdown
INSERT INTO blog.posts (title, slug, content, excerpt, author, published_at, tags)
VALUES (
    'Building an AI-Powered Chatbot with Spring AI',
    'ai-chatbot-spring-ai',
    '# Building an AI-Powered Chatbot with Spring AI

## Introduction

In this post, I''ll walk through...',
    'How I integrated an AI chatbot using Spring AI 1.1.0 with Google Gemini and OpenAI support.',
    'Michael Kopp',
    '2025-12-04 10:00:00',
    ARRAY['spring-ai', 'gemini', 'ai', 'chatbot']
);
```

**Workflow:**
1. Write blog posts in Markdown (`.md` files)
2. Convert Markdown to SQL `INSERT` statements (manual or scripted)
3. Run Flyway migration to populate database
4. Blog posts appear immediately on the website

**Benefits:**
- Version-controlled content (Markdown in Git)
- Database-backed for dynamic queries
- No manual database operations

## Backend Implementation

### JPA Entities

```java
@Entity
@Table(name = "posts", schema = "blog")
public class BlogPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String excerpt;

    @Column(nullable = false)
    private String author;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(length = 20)
    private String status = "PUBLISHED";

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "text[]")
    private List<String> tags;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
}
```

### REST API Endpoints

```java
@RestController
@RequestMapping("/api/blog")
public class BlogController {

    private final BlogService blogService;

    @GetMapping("/posts")
    public Page<BlogPostDTO> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return blogService.findAllPublished(PageRequest.of(page, size));
    }

    @GetMapping("/posts/{slug}")
    public BlogPostDTO getPostBySlug(@PathVariable String slug) {
        return blogService.findBySlug(slug);
    }

    @PostMapping("/posts/{postId}/comments")
    public CommentDTO addComment(
            @PathVariable Long postId,
            @RequestBody CreateCommentRequest request
    ) {
        return blogService.addComment(postId, request);
    }

    @PostMapping("/posts/{postId}/like")
    public LikeDTO toggleLike(
            @PathVariable Long postId,
            @RequestParam String userId
    ) {
        return blogService.toggleLike(postId, userId);
    }
}
```

## Frontend Implementation

### Angular SSR Resolver

The `BlogPostResolver` retrieves blog posts server-side for SEO:

```typescript
@Injectable({ providedIn: 'root' })
export class BlogPostResolver implements Resolve<BlogPostDTO> {
  private blogService = inject(BlogControllerService);

  resolve(route: ActivatedRouteSnapshot): Observable<BlogPostDTO> {
    const slug = route.paramMap.get('slug');
    if (!slug) {
      throw new Error('Missing blog post slug');
    }

    return this.blogService.getPostBySlug({ slug }).pipe(
      catchError((err) => {
        console.error('Failed to load blog post:', err);
        return of(null as any);
      })
    );
  }
}
```

**Route Configuration:**

```typescript
const routes: Routes = [
  {
    path: 'blog/:slug',
    component: BlogPostComponent,
    resolve: { post: BlogPostResolver }
  }
];
```

**Benefits:**
- Blog post content rendered server-side (SEO-friendly)
- Fast initial page load (no client-side API call)
- Progressive enhancement (works without JavaScript)

### Markdown Rendering

The `BlogPostComponent` renders Markdown with syntax highlighting:

```typescript
@Component({
  selector: 'app-blog-post',
  template: `
    <article class="blog-post">
      <h1>{{ post().title }}</h1>
      <div class="metadata">
        <span>{{ post().author }}</span>
        <span>{{ post().publishedAt | date }}</span>
      </div>
      
      <!-- Markdown rendering -->
      <div class="content" markdown [data]="post().content"></div>
      
      <!-- Comments section -->
      <app-comment-section [postId]="post().id" />
      
      <!-- Like button -->
      <app-blog-like-button [postId]="post().id" />
    </article>
  `,
  standalone: true,
  imports: [MarkdownModule, CommonModule, CommentSectionComponent, BlogLikeButtonComponent]
})
export class BlogPostComponent {
  post = input.required<BlogPostDTO>();
}
```

**Markdown Configuration:**

```typescript
// app.config.ts
import { provideMarkdown } from 'ngx-markdown';

export const appConfig: ApplicationConfig = {
  providers: [
    provideMarkdown({
      markedOptions: {
        provide: MarkedOptions,
        useValue: {
          gfm: true,
          breaks: false,
          pedantic: false,
          smartLists: true,
          smartypants: false,
        },
      },
    }),
  ],
};
```

For syntax highlighting details, see [ADR-010: Markdown Rendering with highlight.js](../adr/0010-markdown-rendering-with-highlightjs.md).

### Comment System

The comment system supports nested/threaded comments:

```typescript
@Component({
  selector: 'app-comment-section',
  template: `
    <section class="comments">
      <h3>Comments ({{ comments().length }})</h3>
      
      <!-- New comment form -->
      <app-comment-form 
        [postId]="postId()" 
        (commentAdded)="loadComments()" />
      
      <!-- Comment list (recursive for nesting) -->
      <div class="comment-list">
        @for (comment of comments(); track comment.id) {
          <app-comment-item 
            [comment]="comment" 
            [postId]="postId()"
            (replyAdded)="loadComments()" />
        }
      </div>
    </section>
  `,
  standalone: true,
  imports: [CommentFormComponent, CommentItemComponent]
})
export class CommentSectionComponent implements OnInit {
  postId = input.required<number>();
  private blogService = inject(BlogControllerService);
  
  comments = signal<CommentDTO[]>([]);

  ngOnInit(): void {
    this.loadComments();
  }

  loadComments(): void {
    this.blogService.getComments({ postId: this.postId() })
      .subscribe(comments => this.comments.set(comments));
  }
}
```

**Nested Comment Rendering:**

```typescript
@Component({
  selector: 'app-comment-item',
  template: `
    <div class="comment" [class.nested]="comment().parentId">
      <div class="comment-header">
        <span class="author">{{ comment().author }}</span>
        <span class="date">{{ comment().createdAt | date }}</span>
      </div>
      <div class="comment-body">{{ comment().content }}</div>
      
      <!-- Reply button -->
      <button (click)="showReplyForm.set(!showReplyForm())">Reply</button>
      
      <!-- Reply form (toggle) -->
      @if (showReplyForm()) {
        <app-comment-form 
          [postId]="postId()" 
          [parentId]="comment().id"
          (commentAdded)="handleReply()" />
      }
      
      <!-- Nested replies (recursive) -->
      @if (comment().replies && comment().replies.length > 0) {
        <div class="replies">
          @for (reply of comment().replies; track reply.id) {
            <app-comment-item 
              [comment]="reply" 
              [postId]="postId()"
              (replyAdded)="replyAdded.emit()" />
          }
        </div>
      }
    </div>
  `
})
export class CommentItemComponent {
  comment = input.required<CommentDTO>();
  postId = input.required<number>();
  showReplyForm = signal(false);
  
  @Output() replyAdded = new EventEmitter<void>();

  handleReply(): void {
    this.showReplyForm.set(false);
    this.replyAdded.emit();
  }
}
```

### Like System

The like button uses optimistic UI updates:

```typescript
@Component({
  selector: 'app-blog-like-button',
  template: `
    <button 
      class="like-button" 
      [class.liked]="isLiked()"
      (click)="toggleLike()">
      <i class="fa-heart" [class.fa-solid]="isLiked()" [class.fa-regular]="!isLiked()"></i>
      <span>{{ likeCount() }}</span>
    </button>
  `
})
export class BlogLikeButtonComponent implements OnInit {
  postId = input.required<number>();
  private blogService = inject(BlogControllerService);
  private authService = inject(AuthService);
  
  likeCount = signal(0);
  isLiked = signal(false);

  ngOnInit(): void {
    this.loadLikeStatus();
  }

  toggleLike(): void {
    const userId = this.authService.currentUser()?.id;
    if (!userId) {
      // Redirect to login
      return;
    }

    // Optimistic update
    const wasLiked = this.isLiked();
    this.isLiked.set(!wasLiked);
    this.likeCount.update(count => wasLiked ? count - 1 : count + 1);

    // API call
    this.blogService.toggleLike({ 
      postId: this.postId(), 
      userId 
    }).subscribe({
      error: () => {
        // Revert on error
        this.isLiked.set(wasLiked);
        this.likeCount.update(count => wasLiked ? count + 1 : count - 1);
      }
    });
  }
}
```

## Performance Optimizations

### 1. Database Indexing
```sql
CREATE INDEX idx_posts_published_at ON blog.posts(published_at DESC);
CREATE INDEX idx_comments_post_id ON blog.comments(post_id);
```

### 2. Pagination
```java
@GetMapping("/posts")
public Page<BlogPostDTO> getAllPosts(Pageable pageable) {
    return blogService.findAllPublished(pageable);
}
```

### 3. Query Optimization
```java
@Query("SELECT p FROM BlogPost p LEFT JOIN FETCH p.comments WHERE p.slug = :slug")
Optional<BlogPost> findBySlugWithComments(@Param("slug") String slug);
```

### 4. Caching (Planned)
- Redis cache for popular blog posts
- Cache invalidation on comment/like updates
- Aggressive caching with short TTL (5-10 minutes)

## Challenges and Solutions

### Challenge 1: Markdown in SQL
**Problem:** Escaping single quotes in Markdown content for SQL `INSERT` statements.

**Solution:** Use PostgreSQL's `$$` dollar-quoted strings:

```sql
INSERT INTO blog.posts (content) VALUES ($$
# Title with 'single quotes'

Content with "double quotes" and $variables$
$$);
```

### Challenge 2: SSR Hydration Mismatch
**Problem:** Server-rendered HTML doesn't match client-rendered HTML (different timestamps, user state).

**Solution:** Use Angular's `isPlatformBrowser` to conditionally render:

```typescript
if (isPlatformBrowser(this.platformId)) {
  // Client-only code (e.g., localStorage, window)
}
```

### Challenge 3: Comment Nesting Depth
**Problem:** Deeply nested comments (10+ levels) are hard to render.

**Solution:** Limit nesting depth to 3 levels:

```typescript
<app-comment-item 
  [comment]="comment" 
  [depth]="0" 
  [maxDepth]="3" />
```

## Future Enhancements

1. **Comment Moderation:** Admin dashboard for approving/rejecting comments
2. **Markdown Editor:** Rich text editor with live preview for blog post creation
3. **Search:** Full-text search with PostgreSQL `tsvector`
4. **RSS Feed:** Auto-generated RSS feed for blog posts
5. **Related Posts:** Recommendation engine based on tags
6. **Reading Time Estimate:** Calculate reading time from word count

## Conclusion

The database-driven blog system combines the best of both worlds: developer-friendly Markdown authoring and dynamic database-backed features. By leveraging Flyway migrations, Angular SSR, and PostgreSQL, the blog achieves excellent SEO, fast performance, and rich interactivity.

The architecture is designed to scale from a simple blog to a full-featured content management system with minimal refactoring.

## Resources

- [ADR-012: SSR Blog Optimization with Database](../adr/0012-ssr-blog-optimization-with-database.md)
- [ADR-010: Markdown Rendering with highlight.js](../adr/0010-markdown-rendering-with-highlightjs.md)
- [ADR-006: Flyway Over Liquibase](../adr/0006-flyway-over-liquibase.md)
- [Flyway Migration: V1_1_2__init_blog.sql](../../apps/mysite-backend/src/main/resources/db/migration/V1_1_2__init_blog.sql)
- [Flyway Migration: V1_1_4__seed_blog_posts.sql](../../apps/mysite-backend/src/main/resources/db/migration/V1_1_4__seed_blog_posts.sql)

---

**Next Post:** Implementing a No-Flash Theme System with Early JavaScript Loading
