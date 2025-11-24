-- Create table for comment likes

CREATE TABLE blog.comment_likes (
    id UUID NOT NULL,
    comment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_comment_likes PRIMARY KEY (id),
    CONSTRAINT uk_comment_user UNIQUE (comment_id, user_id),
    CONSTRAINT fk_comment_like_comment FOREIGN KEY (comment_id) REFERENCES blog.comments (id) ON DELETE CASCADE
);