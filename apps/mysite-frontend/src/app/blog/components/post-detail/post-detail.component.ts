import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MarkdownModule } from 'ngx-markdown';
import { BlogPostsService } from '../../services/blog-posts.service';
import { BlogPost } from '../../models/post.model';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faArrowLeft, faCalendar, faUser, faTag } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'mysite-post-detail',
  standalone: true,
  imports: [CommonModule, MarkdownModule, FontAwesomeModule],
  templateUrl: './post-detail.component.html'
})
export class PostDetailComponent implements OnInit {

  private route: ActivatedRoute = inject(ActivatedRoute);
  private router: Router = inject(Router);
  private blogService: BlogPostsService = inject(BlogPostsService);

  post = signal<BlogPost | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  faArrowLeft = faArrowLeft;
  faCalendar = faCalendar;
  faUser = faUser;
  faTag = faTag;

  ngOnInit() {
    const slug = this.route.snapshot.paramMap.get('slug');
    if (!slug) {
      this.error.set('Post not found');
      this.loading.set(false);
      return;
    }

    this.blogService.getPostBySlug(slug).subscribe({
      next: (post) => {
        this.post.set(post);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Failed to load post');
        this.loading.set(false);
      }
    });
  }

  goBack() {
    this.router.navigate(['/blog']);
  }

  onMarkdownReady() {
    if (typeof window !== 'undefined' && (window as any).mermaid) {
      (window as any).mermaid.init(undefined, '.language-mermaid');
    }
  }
}
