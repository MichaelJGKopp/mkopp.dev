import { Component, Input, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faHeart as faHeartSolid } from '@fortawesome/free-solid-svg-icons';
import {
  faHeart as faHeartRegular,
  faShareSquare,
} from '@fortawesome/free-regular-svg-icons';
import { Oauth2AuthService } from '../../../auth/oauth2-auth.service';
import { BlogPostsService } from '../../services/blog-posts.service';

@Component({
  selector: 'mysite-blog-like-button',
  standalone: true,
  imports: [CommonModule, FontAwesomeModule],
  templateUrl: './blog-like-button.component.html',
})
export class BlogLikeButtonComponent implements OnInit {
  @Input({ required: true }) blogPostId!: string;

  private readonly blogPostsService = inject(BlogPostsService);
  private readonly authService = inject(Oauth2AuthService);

  faHeartSolid = faHeartSolid;
  faHeartRegular = faHeartRegular;
  faShareSquare = faShareSquare;

  liked = signal(false);
  likeCount = signal(0);
  togglingLike = signal(false);
  isAuthenticated = this.authService.isAuthenticated;

  async ngOnInit() {
    this.loadLikeInfo();
  }

  async loadLikeInfo() {
    try {
      const isLiked = this.isAuthenticated()
        ? await this.blogPostsService.isLikedByCurrentUser(this.blogPostId)
        : false;
      const count = await this.blogPostsService.getLikeCount(this.blogPostId);

      this.liked.set(isLiked);
      this.likeCount.set(count);
    } catch (error) {
      console.error('Failed to load like info:', error);
    }
  }

  async toggleLike() {
    if (!this.isAuthenticated() || this.togglingLike()) {
      return;
    }

    this.togglingLike.set(true);

    try {
      await this.blogPostsService.toggleLike(this.blogPostId);

      // Toggle the local state optimistically
      const newLikedState = !this.liked();
      this.liked.set(newLikedState);
      this.likeCount.update((count) => (newLikedState ? count + 1 : count - 1));
    } catch (error) {
      console.error('Failed to toggle like:', error);
      // Reload to get accurate state
      await this.loadLikeInfo();
    } finally {
      this.togglingLike.set(false);
    }
  }

  async share() {
    const url = window.location.href;

    if (navigator.share) {
      try {
        await navigator.share({
          title: 'Check out this blog post',
          url: url,
        });
      } catch (error) {
        if ((error as Error).name !== 'AbortError') {
          console.error('Failed to share:', error);
        }
      }
    } else {
      // Fallback to clipboard
      try {
        await navigator.clipboard.writeText(url);
        alert('Link copied to clipboard!');
      } catch (error) {
        console.error('Failed to copy to clipboard:', error);
      }
    }
  }

  login() {
    this.authService.login();
  }
}
