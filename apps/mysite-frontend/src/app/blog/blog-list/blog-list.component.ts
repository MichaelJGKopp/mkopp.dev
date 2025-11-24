import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PostCardComponent } from '../components/post-card/post-card.component';
import { BlogPostResponse } from '@mkopp/api-clients/backend';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'mysite-blog-list',
  standalone: true,
  imports: [CommonModule, PostCardComponent],
  templateUrl: './blog-list.component.html',
})
export class BlogListComponent implements OnInit {
  private route = inject(ActivatedRoute);
  posts: BlogPostResponse[] = [];

  ngOnInit() {
    this.route.data.subscribe((data) => {
      this.posts = data['posts'] || [];
    });
  }
}
