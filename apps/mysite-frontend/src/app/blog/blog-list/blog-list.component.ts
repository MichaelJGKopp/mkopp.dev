import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PostCardComponent } from '../components/post-card/post-card.component';
import { BlogPostMetadata } from '../models/post.model';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'mysite-blog-list',
  standalone: true,
  imports: [CommonModule, PostCardComponent],
  templateUrl: './blog-list.component.html',
  styleUrls: ['./blog-list.component.scss'],
})
export class BlogListComponent implements OnInit {
  private route = inject(ActivatedRoute);
  posts: BlogPostMetadata[] = [];

  ngOnInit() {
    this.route.data.subscribe(data => {
      this.posts = data['posts'] || [];
    });
  }
}