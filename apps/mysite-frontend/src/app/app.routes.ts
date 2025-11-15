import { Route } from '@angular/router';
import { BlogListComponent } from './blog/blog-list/blog-list.component';
import { PostDetailComponent } from './blog/components/post-detail/post-detail.component';
import { blogResolver } from './blog/blog.resolver';

export const routes: Route[] = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'blog',
  },
  {
    path: 'blog',
    resolve: { posts: blogResolver },
    children: [
      {
        path: '',
        component: BlogListComponent,
      },
      { path: ':slug', component: PostDetailComponent },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
