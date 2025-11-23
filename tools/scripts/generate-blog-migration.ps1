# Script to generate SQL migration file from blog posts
$postsIndexPath = "apps\mysite-frontend\src\assets\blog\posts-index.json"
$postsDir = "apps\mysite-frontend\src\assets\blog\posts"
$outputPath = "apps\mysite-backend\src\main\resources\db\migration\V1_1_4__seed_blog_posts.sql"

# Read posts metadata
$postsMetadata = Get-Content $postsIndexPath | ConvertFrom-Json

# Get a test user UUID (we'll use a fixed UUID for the author)
$authorId = "00000000-0000-0000-0000-000000000001"

$sqlContent = @"
-- Seed blog posts data
-- This migration inserts initial blog posts from the existing markdown content

-- Create a default author if not exists (for blog posts)
INSERT INTO app_user.users (id, username, email, first_name, last_name, created_at, updated_at)
VALUES ('$authorId', 'michael.kopp', 'michaeljg.kopp@gmail.com', 'Michael', 'Kopp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

"@

$postNumber = 1
foreach ($post in $postsMetadata) {
    $slug = $post.slug
    $title = $post.title -replace "'", "''"
    $description = $post.description -replace "'", "''"
    $publishedAt = $post.publishedAt
    $type = $post.type.ToUpper()
    
    # Read markdown content
    $mdPath = Join-Path $postsDir "$slug.md"
    if (Test-Path $mdPath) {
        $content = Get-Content $mdPath -Raw -Encoding UTF8
        # Escape single quotes by doubling them
        $content = $content -replace "'", "''"
        # Normalize line endings
        $content = $content -replace "`r`n", "`n"
        # Escape dollar signs for Flyway placeholders by doubling them
        $content = $content -replace '\$', '$$$$'
    } else {
        $content = ""
    }
    
    # Generate UUID for blog post (deterministic based on slug)
    $postId = [guid]::NewGuid().ToString()
    
    $sqlContent += @"
-- Blog Post $postNumber`: $title
INSERT INTO blog.blog_posts (id, slug, title, description, content, author_id, published_at, created_at, updated_at, type)
VALUES (
    '$postId',
    '$slug',
    '$title',
    '$description',
    '$content',
    '$authorId',
    '$publishedAt'::TIMESTAMPTZ,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '$type'
);

"@
    
    # Insert tags
    foreach ($tag in $post.tags) {
        $tagName = $tag -replace "'", "''"
        $tagId = [guid]::NewGuid().ToString()
        
        $sqlContent += @"
-- Tag: $tagName
INSERT INTO blog.blog_tags (id, name, created_at, updated_at)
VALUES ('$tagId', '$tagName', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO blog.blog_post_tags (blog_post_id, tag_id)
SELECT '$postId', id FROM blog.blog_tags WHERE name = '$tagName';

"@
    }
    
    $postNumber++
}

# Write to file with proper UTF-8 encoding
[System.IO.File]::WriteAllText($outputPath, $sqlContent, [System.Text.UTF8Encoding]::new($false))

Write-Host "✅ SQL migration file generated at $outputPath" -ForegroundColor Green
Write-Host "Total posts: $($postsMetadata.Count)" -ForegroundColor Cyan
