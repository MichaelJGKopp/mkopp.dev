import { inject } from '@angular/core';
import { BlogPostsService } from './services/blog-posts.service';

export const blogResolver = () => {
  const blogService = inject(BlogPostsService);
  return blogService.loadPostsMetadataSSR();
};
