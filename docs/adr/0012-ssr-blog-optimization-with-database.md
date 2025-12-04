# 📄 ADR 012: SSR Blog Optimization with Database Storage

## Status

**Accepted** (2025-12-04)

## Context

The blog system initially loaded Markdown files from the filesystem and rendered them client-side. This approach had several limitations:

* Poor SEO - content not in initial HTML response
* Slower perceived performance - client-side rendering delay
* Difficult content management - file-based storage
* No metadata indexing - couldn't query by tags, dates, etc.
* Challenging deployment - Markdown files in Docker image

Requirements:

* SEO-friendly: blog content in initial server response
* Fast Time-to-First-Byte (TTFB)
* Searchable and filterable content
* Centralized content management
* Support for comments and likes
* Database-backed for reliability

## Decision

We will **store blog posts in PostgreSQL** and **retrieve them server-side during SSR**, then render the Markdown content using ngx-markdown on both server and client.

Implementation:

* **Storage**: Blog posts stored as Markdown in PostgreSQL `blog.blog_posts` table
* **Migration**: Flyway migration (`V1_1_4__seed_blog_posts.sql`) inserts existing Markdown posts
* **SSR Retrieval**: Angular resolver fetches posts server-side before page renders
* **API Optimization**: Backend returns HTML-ready Markdown
* **Rendering**: ngx-markdown renders content on server (SSR) and client (hydration)
* **Metadata**: Tags, publish dates, descriptions stored as structured data
* **Indexing**: PostgreSQL indexes on slug, tags, publish date for fast queries

**Architecture Flow:**

```
1. User requests /blog/my-post-slug
2. Angular SSR resolver calls backend API
3. Backend queries PostgreSQL for post by slug
4. Returns BlogPostResponse with Markdown content
5. Angular SSR renders page with ngx-markdown
6. HTML with blog content sent to browser (SEO-friendly)
7. Angular hydrates and makes page interactive
```

**Database Schema:**

```sql
CREATE TABLE blog.blog_posts (
    id UUID PRIMARY KEY,
    slug VARCHAR(255) UNIQUE,
    title VARCHAR(500),
    description TEXT,
    content TEXT,  -- Markdown stored here
    author_id UUID,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    thumbnail_url VARCHAR(1000),
    type VARCHAR(50),
    external_url VARCHAR(1000)
);
```

**Angular Resolver:**

```typescript
export const blogResolver: ResolveFn<BlogPostResponse> = (route) => {
  const blogService = inject(BlogService);
  const slug = route.paramMap.get('slug')!;
  return blogService.getPostBySlug(slug);
};

// Route configuration
{
  path: 'blog/:slug',
  component: PostDetailComponent,
  resolve: { post: blogResolver }  // Executes server-side!
}
```

**Component:**

```typescript
export class PostDetailComponent {
  post = input.required<BlogPostResponse>();  // From resolver
  
  // Content already available on first render (SSR)
  // No loading spinner needed!
}
```

## Alternatives Considered

### Static Site Generation (SSG)

* ✅ Fastest possible TTFB
* ✅ No database queries at runtime
* ❌ Requires rebuild for every content update
* ❌ Not suitable for frequently updated blogs
* ❌ Can't support dynamic features (comments, likes)

### Client-Side Only Rendering

* ✅ Simple implementation
* ✅ No SSR complexity
* ❌ **Poor SEO** - content not in HTML
* ❌ Slow perceived performance
* ❌ Flash of loading state
* ❌ Not suitable for content-focused site

### CMS Integration (Contentful, Strapi)

* ✅ Rich content management UI
* ✅ Built-in media management
* ❌ External dependency
* ❌ Monthly cost (for managed services)
* ❌ Over-engineered for single-author blog
* ❌ Less control over data structure

### SSR + Database Storage (Chosen)

* ✅ **Excellent SEO** - content in initial HTML
* ✅ Fast perceived performance
* ✅ Flexible data model
* ✅ Supports dynamic features (comments, likes, search)
* ✅ Centralized content management
* ✅ Version control via Flyway migrations
* ✅ Full control over implementation
* ❌ Requires Angular Universal (already implemented)
* ❌ Database queries on every request (mitigated with caching)

## Consequences

### Positive

* **SEO Excellence**: Blog content in initial server response, crawlers see full content
* **Performance**: No client-side data fetching delay for initial render
* **No Loading States**: Content available immediately, better UX
* **Searchable**: Can query blog posts by tags, dates, keywords
* **Structured Data**: Metadata (tags, descriptions) stored relationally
* **Dynamic Features**: Comments and likes tied to blog posts via foreign keys
* **Migration Automation**: Flyway script converts existing Markdown files to SQL
* **Version Control**: Database migrations tracked in Git
* **Scalability**: Can add full-text search, recommendations, etc.

### Negative

* **Database Load**: Every page view queries database (mitigated with indexes and future caching)
* **Deployment Complexity**: Must run Flyway migrations before app starts
* **Content Editing**: Need admin UI or direct database access (planned admin panel)
* **Migration Overhead**: New blog posts need SQL migration or admin UI

### Implementation Notes

**Current Implementation:**
* Blog posts stored in PostgreSQL
* Flyway migration seeds initial posts from Markdown
* Angular resolver fetches post server-side
* ngx-markdown renders on server and client
* Slug-based URLs for SEO (e.g., `/blog/spring-ai-integration`)
* Comments and likes linked via foreign keys

**Performance Optimizations:**
* PostgreSQL index on `slug` column for fast lookup
* Index on `published_at` for blog list sorting
* Pagination to limit query size
* Connection pooling for database efficiency

**Future Enhancements:**
* Redis caching layer for frequently accessed posts
* CDN caching of blog pages (with cache invalidation)
* Full-text search with PostgreSQL `tsvector`
* Admin UI for creating/editing posts (no direct DB access needed)
* Scheduled publishing (posts with future `published_at`)
* Draft mode for unpublished posts

**SEO Benefits:**
* HTML contains full blog post content
* Search engines can index entire post
* Meta tags (title, description) in <head>
* Structured data for rich snippets
* Fast TTFB improves search ranking

**Migration Example:**

```sql
-- V1_1_4__seed_blog_posts.sql
INSERT INTO blog.blog_posts (id, slug, title, content, ...)
VALUES (
  'uuid-here',
  'spring-ai-integration',
  'Implementing AI with Spring AI',
  '# Spring AI Integration\n\nFull markdown content here...',
  ...
);
```

## Related Documents

* [Design Document v0.4](../design.md)
* [ADR 003 – SSR Angular](./0003-ssr-angular.md)
* [ADR 006 – Flyway Over Liquibase](./0006-flyway-over-liquibase.md)
* [ADR 010 – Markdown Rendering](./0010-markdown-rendering-with-highlightjs.md)
* [Flyway Migration V1_1_4](../../apps/mysite-backend/src/main/resources/db/migration/V1_1_4__seed_blog_posts.sql)
