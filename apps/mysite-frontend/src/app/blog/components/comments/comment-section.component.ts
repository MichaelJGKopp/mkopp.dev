import { Component, Input, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { CommentsService, CommentTreeItem } from '@mkopp/api-clients/backend';
import { Oauth2AuthService } from '../../../auth/oauth2-auth.service';
import { CommentItemComponent } from './comment-item.component';

@Component({
  selector: 'mysite-comment-section',
  standalone: true,
  imports: [CommonModule, FormsModule, FontAwesomeModule, CommentItemComponent],
  templateUrl: './comment-section.component.html',
})
export class CommentSectionComponent implements OnInit {
  private commentsService = inject(CommentsService);
  private authService = inject(Oauth2AuthService);

  @Input({ required: true }) blogPostId!: string;

  comments = signal<CommentTreeItem[]>([]);
  totalComments = signal(0);
  currentPage = signal(0);
  pageSize = 10;
  hasMoreComments = signal(false);

  loadingComments = signal(false);
  loadingMore = signal(false);
  submittingComment = signal(false);

  newCommentContent = '';

  isAuthenticated = this.authService.isAuthenticated;
  currentUserId = signal<string | undefined>(undefined);

  ngOnInit() {
    this.loadComments();
    this.loadCommentCount();
  }

  private loadComments() {
    this.loadingComments.set(true);

    this.commentsService
      .getTopLevelComments(this.blogPostId, this.currentPage(), this.pageSize, [
        'createdAt,desc',
      ])
      .subscribe({
        next: (response) => {
          this.comments.set(response.content || []);
          this.hasMoreComments.set(!response.last);
          this.loadingComments.set(false);
        },
        error: (err) => {
          console.error('Failed to load comments:', err);
          this.loadingComments.set(false);
        },
      });
  }

  private loadCommentCount() {
    this.commentsService.getCommentCount(this.blogPostId).subscribe({
      next: (count) => this.totalComments.set(count),
      error: (err) => console.error('Failed to load comment count:', err),
    });
  }

  loadMoreComments() {
    this.loadingMore.set(true);
    this.currentPage.update((p) => p + 1);

    this.commentsService
      .getTopLevelComments(this.blogPostId, this.currentPage(), this.pageSize, [
        'createdAt,desc',
      ])
      .subscribe({
        next: (response) => {
          this.comments.update((existing) => [
            ...existing,
            ...(response.content || []),
          ]);
          this.hasMoreComments.set(!response.last);
          this.loadingMore.set(false);
        },
        error: (err) => {
          console.error('Failed to load more comments:', err);
          this.loadingMore.set(false);
          this.currentPage.update((p) => p - 1);
        },
      });
  }

  submitComment() {
    if (!this.newCommentContent.trim()) return;

    this.submittingComment.set(true);

    this.commentsService
      .createComment(this.blogPostId, {
        content: this.newCommentContent,
        parentCommentId: undefined,
      })
      .subscribe({
        next: (newComment) => {
          this.comments.update((existing) => [newComment, ...existing]);
          this.totalComments.update((count) => count + 1);
          this.newCommentContent = '';
          this.submittingComment.set(false);
        },
        error: (err) => {
          console.error('Failed to post comment:', err);
          this.submittingComment.set(false);
        },
      });
  }

  handleCommentDeleted(event: { commentId: string }) {
    this.comments.update((existing) =>
      existing.filter((c) => c.id !== event.commentId)
    );
    this.totalComments.update((count) => Math.max(0, count - 1));
  }

  handleCommentUpdated(event: { comment: CommentTreeItem }) {
    this.comments.update((existing) =>
      existing.map((c) => (c.id === event.comment.id ? event.comment : c))
    );
  }

  handleReplyAdded(event: { commentId: string }) {
    this.totalComments.update((count) => count + 1);
    // Increment reply count for the parent comment
    this.comments.update((existing) =>
      existing.map((c) =>
        c.id === event.commentId
          ? { ...c, replyCount: (c.replyCount || 0) + 1 }
          : c
      )
    );
  }

  login() {
    this.authService.login();
  }
}
