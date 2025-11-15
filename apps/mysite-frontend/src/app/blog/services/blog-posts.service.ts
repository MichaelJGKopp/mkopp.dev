import {
  inject,
  Injectable,
  makeStateKey,
  signal,
  TransferState,
} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, map, of } from 'rxjs';
import { BlogPost, BlogPostMetadata } from '../models/post.model';

const POSTS_KEY = makeStateKey<any[]>('posts');

@Injectable({
  providedIn: 'root',
})
export class BlogPostsService {
  private postsMetadata = signal<BlogPostMetadata[]>([]);
  private http = inject(HttpClient);
  private transferState = inject(TransferState);

  loadPostsMetadataSSR(): Observable<BlogPostMetadata[]> {
    return this.http
      .get<BlogPostMetadata[]>('/assets/blog/posts-index.json')
      .pipe(
        tap((metadata) => {
          this.postsMetadata.set(metadata);
          this.transferState.set(POSTS_KEY, metadata);
        })
      );
  }

  getAllPosts(): BlogPostMetadata[] {
    return this.postsMetadata();
  }

  getPostBySlug(slug: string): Observable<BlogPost> {
    const metadata = this.postsMetadata().find((p) => p.slug === slug);

    if (!metadata) {
      throw new Error(`Post with slug "${slug}" not found`);
    }

    return this.http
      .get(`/assets/blog/posts/${slug}.md`, { responseType: 'text' })
      .pipe(
        map((content) => ({
          ...metadata,
          content,
          publishedAt: new Date(metadata.publishedAt),
        }))
      );
  }

  getRecentPosts(limit = 99): BlogPostMetadata[] {
    return [...this.postsMetadata()]
      .sort(
        (a, b) =>
          new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime()
      )
      .slice(0, limit);
  }
}
