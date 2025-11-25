import { ThemeService } from './../../../shared/theme/theme.service';
import {
  Component,
  OnInit,
  signal,
  PLATFORM_ID,
  inject,
  afterNextRender,
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MarkdownModule } from 'ngx-markdown';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import {
  faArrowLeft,
  faCalendar,
  faUser,
  faTag,
} from '@fortawesome/free-solid-svg-icons';
import { BlogPostsService } from '../../services/blog-posts.service';
import { CommentSectionComponent } from '../comments/comment-section.component';
import { BlogLikeButtonComponent } from '../blog-like-button/blog-like-button.component';

@Component({
  selector: 'mysite-post-detail',
  standalone: true,
  imports: [
    CommonModule,
    MarkdownModule,
    FontAwesomeModule,
    CommentSectionComponent,
    BlogLikeButtonComponent,
  ],
  templateUrl: './post-detail.component.html',
})
export class PostDetailComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);
  private route: ActivatedRoute = inject(ActivatedRoute);
  private router: Router = inject(Router);
  private blogService: BlogPostsService = inject(BlogPostsService);
  private themeService = inject(ThemeService);

  isDarkMode = this.themeService.isDarkMode;
  loading = signal(true);
  error = signal<string | null>(null);
  post = signal<any | null>(null);

  faArrowLeft = faArrowLeft;
  faCalendar = faCalendar;
  faUser = faUser;
  faTag = faTag;

  constructor() {
    // Initialize mermaid after render (client-side only)
    afterNextRender(() => {
      this.initializeMermaid();
    });
  }

  ngOnInit() {
    this.loadPost();
  }

  private loadPost() {
    this.loading.set(true);
    this.error.set(null);

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
      },
    });
  }

  goBack() {
    this.router.navigate(['/blog']);
  }

  onMarkdownReady() {
    if (isPlatformBrowser(this.platformId)) {
      // Re-run syntax highlighting
      setTimeout(() => {
        if ((window as any).hljs) {
          (window as any).hljs.highlightAll();
        }
        this.initializeMermaid();
      }, 100);
    }
  }

  private initializeMermaid() {
    if (isPlatformBrowser(this.platformId) && (window as any).mermaid) {
      try {
        (window as any).mermaid.run({
          querySelector: '.language-mermaid',
        });
      } catch (e) {
        console.error('Mermaid initialization failed:', e);
      }
    }
  }
}
