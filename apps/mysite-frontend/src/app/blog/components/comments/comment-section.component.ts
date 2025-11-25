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
  template: `
    <div class="mt-12 w-full">
      <div class="divider"></div>

      <div class="mb-6 flex items-center justify-between">
        <h2 class="text-2xl font-bold">Comments ({{ totalComments() }})</h2>
      </div>

      <!-- New Comment Form -->
      @if (isAuthenticated()) {
        <div class="card mb-6 bg-base-200 shadow-sm">
          <div class="card-body">
            <textarea
              class="textarea textarea-bordered w-full"
              placeholder="Write a comment..."
              rows="3"
              [(ngModel)]="newCommentContent"
              [disabled]="submittingComment()"
            ></textarea>
            <div class="card-actions mt-2 justify-end">
              <button
                class="btn btn-primary btn-sm"
                (click)="submitComment()"
                [disabled]="!newCommentContent.trim() || submittingComment()"
              >
                @if (submittingComment()) {
                  <span class="loading loading-spinner loading-xs"></span>
                }
                Post Comment
              </button>
            </div>
          </div>
        </div>
      } @else {
        <div class="alert alert-info mb-6">
          <span>Please log in to post comments</span>
        </div>
      }

      <!-- Comments List -->
      @if (loadingComments()) {
        <div class="flex justify-center py-8">
          <span class="loading loading-spinner loading-lg"></span>
        </div>
      } @else if (comments().length === 0) {
        <div class="py-8 text-center text-base-content/60">
          No comments yet. Be the first to comment!
        </div>
      } @else {
        <div class="space-y-4">
          @for (comment of comments(); track comment.id) {
            <mysite-comment-item
              [comment]="comment"
              [blogPostId]="blogPostId"
              [currentUserId]="currentUserId()"
              [isAuthenticated]="isAuthenticated()"
              (commentDeleted)="handleCommentDeleted($event)"
              (commentUpdated)="handleCommentUpdated($event)"
              (replyAdded)="handleReplyAdded($event)"
            />
          }
        </div>

        <!-- Load More -->
        @if (hasMoreComments()) {
          <div class="mt-6 flex justify-center">
            <button
              class="btn btn-outline"
              (click)="loadMoreComments()"
              [disabled]="loadingMore()"
            >
              @if (loadingMore()) {
                <span class="loading loading-spinner loading-sm"></span>
              }
              Load More Comments
            </button>
          </div>
        }
      }
    </div>
  `,
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

  isAuthenticated = signal(false);
  currentUserId = signal<string | undefined>(undefined);

  ngOnInit() {
    this.checkAuthentication();
    this.loadComments();
    this.loadCommentCount();
  }

  private async checkAuthentication() {
    const authenticated = this.authService.isAuthenticated();
    this.isAuthenticated.set(authenticated);

    // TODO: Get user ID from auth service if needed for ownership checks
    // this.currentUserId.set(userId);
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
}
