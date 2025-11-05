import { Route } from '@angular/router';
import { BlogListComponent } from './blog/blog-list/blog-list.component';
import { PostDetailComponent } from './blog/components/post-detail/post-detail.component';

export const routes: Route[] = [
//   {
//     path: '',
//     loadComponent: () => import('./home/home.component').then(m => m.HomeComponent)
//   },
  {
    path: '',
    children: [
      { path: '', component: BlogListComponent }
    ]
  },
  {
    path: 'blog',
    children: [
      { path: '', component: BlogListComponent },
      { path: ':slug', component: PostDetailComponent }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
