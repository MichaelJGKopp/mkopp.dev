import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap, map, lastValueFrom } from 'rxjs';
import {
  BlogService,
  BlogPostResponse,
  LikesService,
} from '@mkopp/api-clients/backend';

@Injectable({
  providedIn: 'root',
})
export class BlogPostsService {
  private postsMetadata = signal<BlogPostResponse[]>([]);
  private blogApi = inject(BlogService);
  private likesApi = inject(LikesService);

  loadPostsMetadataSSR(): Observable<BlogPostResponse[]> {
    return this.blogApi.getAllPosts(0, 100).pipe(
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

  async toggleLike(blogPostId: string): Promise<void> {
    await lastValueFrom(this.likesApi.toggleLike(blogPostId));
  }

  async getLikeCount(blogPostId: string): Promise<number> {
    const response = await lastValueFrom(this.likesApi.getLikeInfo(blogPostId));
    return response.likeCount ?? 0;
  }

  async isLikedByCurrentUser(blogPostId: string): Promise<boolean> {
    const response = await lastValueFrom(this.likesApi.getLikeInfo(blogPostId));
    return response.isLiked ?? false;
  }
}
