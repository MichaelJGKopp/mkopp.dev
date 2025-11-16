CREATE TABLE blog.blog_posts (
    id UUID PRIMARY KEY,
    slug VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    content TEXT NOT NULL,
    author_id UUID NOT NULL,
    published_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    thumbnail_url VARCHAR(1000),
    type VARCHAR(50) NOT NULL DEFAULT 'blog',
    external_url VARCHAR(1000),
    CONSTRAINT fk_blog_posts_author FOREIGN KEY (author_id) REFERENCES user.users (id) ON DELETE CASCADE
);

CREATE TABLE blog.blog_tags (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE blog.blog_post_tags (
    blog_post_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    PRIMARY KEY (blog_post_id, tag_id),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (blog_post_id) REFERENCES blog.blog_posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_tags_tag FOREIGN KEY (tag_id) REFERENCES blog.blog_tags (id) ON DELETE CASCADE
);

CREATE TABLE blog.comments (
    id UUID PRIMARY KEY,
    blog_post_id UUID NOT NULL,
    user_id UUID NOT NULL,
    parent_comment_id UUID NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_post FOREIGN KEY (blog_post_id) REFERENCES blog.blog_posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES user.users (id) ON DELETE CASCADE,
    CONSTRAINT fk_parent_comment FOREIGN KEY (parent_comment_id) REFERENCES blog.comments (id) ON DELETE CASCADE
);

CREATE TABLE blog.blog_likes (
    id UUID PRIMARY KEY,
    blog_post_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_likes_post FOREIGN KEY (blog_post_id) REFERENCES blog.blog_posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES user.users (id) ON DELETE CASCADE,
    CONSTRAINT unique_user_post_like UNIQUE (blog_post_id, user_id)
);

CREATE INDEX idx_blog_posts_slug ON blog.blog_posts (slug);

CREATE INDEX idx_blog_posts_author ON blog.blog_posts (author_id);

CREATE INDEX idx_blog_posts_published ON blog.blog_posts (published_at);

CREATE INDEX idx_comments_post ON blog.comments (blog_post_id);

CREATE INDEX idx_comments_user ON blog.comments (user_id);

CREATE INDEX idx_comments_parent ON blog.comments (parent_comment_id);

CREATE INDEX idx_blog_likes_post ON blog.blog_likes (blog_post_id);

CREATE INDEX idx_blog_likes_user ON blog.blog_likes (user_id);