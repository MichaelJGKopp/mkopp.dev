import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { BlogPost, BlogPostMetadata } from '../models/post.model';

@Injectable({
  providedIn: 'root'
})
export class BlogPostsService {
  private postsMetadata = signal<BlogPostMetadata[]>([]);
  private http: HttpClient = inject(HttpClient);

  constructor() {
    this.loadPostsMetadata();
  }

  private loadPostsMetadata() {
    this.http.get<BlogPostMetadata[]>('/blog/posts-index.json')
      .subscribe({
        next: (metadata) => this.postsMetadata.set(metadata),
        error: (err) => console.error('Error loading posts metadata:', err)
      });
  }

  getAllPosts(): BlogPostMetadata[] {
    return this.postsMetadata();
  }

  getPostBySlug(slug: string): Observable<BlogPost> {
    const metadata = this.postsMetadata().find(p => p.slug === slug);
    
    if (!metadata) {
      throw new Error(`Post with slug "${slug}" not found`);
    }

    return this.http.get(`/blog/posts/${slug}.md`, { responseType: 'text' })
      .pipe(
        map(content => ({
          ...metadata,
          content,
          publishedAt: new Date(metadata.publishedAt),
        }))
      );
  }

  getRecentPosts(limit = 6): BlogPostMetadata[] {
    return this.postsMetadata()
      .sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime())
      .slice(0, limit);
  }
}
