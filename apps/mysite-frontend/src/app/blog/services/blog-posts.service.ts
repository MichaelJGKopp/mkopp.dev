import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap, map } from 'rxjs';
import { BlogService, BlogPostResponse } from '@mkopp/api-clients/backend';

@Injectable({
  providedIn: 'root',
})
export class BlogPostsService {
  private postsMetadata = signal<BlogPostResponse[]>([]);
  private blogApi = inject(BlogService);

  loadPostsMetadataSSR(): Observable<BlogPostResponse[]> {
    return this.blogApi.getAllPosts({ page: 0, size: 100 }).pipe(
      map((response) => response.content || []),
      tap((metadata) => {
        this.postsMetadata.set(metadata);
      })
    );
  }

  getAllPosts(): BlogPostResponse[] {
    return this.postsMetadata();
  }

  getPostBySlug(slug: string): Observable<BlogPostResponse> {
    return this.blogApi.getPostBySlug(slug);
  }

  getRecentPosts(limit = 99): BlogPostResponse[] {
    return [...this.postsMetadata()]
      .sort((a, b) => {
        const dateA = a.publishedAt ? new Date(a.publishedAt).getTime() : 0;
        const dateB = b.publishedAt ? new Date(b.publishedAt).getTime() : 0;
        return dateB - dateA;
      })
      .slice(0, limit);
  }
}
