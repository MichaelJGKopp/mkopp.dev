import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlogPostsService } from '../services/blog-posts.service';
import { PostCardComponent } from '../components/post-card/post-card.component';
import { BlogPostMetadata } from '../models/post.model';

@Component({
  selector: 'mysite-blog-list',
  standalone: true,
  imports: [CommonModule, PostCardComponent],
  templateUrl: './blog-list.component.html',
  styleUrls: ['./blog-list.component.scss'],
})
export class BlogListComponent implements OnInit {
  posts = signal<BlogPostMetadata[]>([]);
  loading = signal(true);
  private blogService: BlogPostsService = inject(BlogPostsService);

  ngOnInit() {
      this.posts.set(this.blogService.getAllPosts());
      this.loading.set(false);
  }
}