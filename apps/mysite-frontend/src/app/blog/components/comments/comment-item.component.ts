import {
  Component,
  Input,
  Output,
  EventEmitter,
  signal,
  inject,
  OnInit,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import {
  faHeart as faHeartSolid,
  faReply,
  faShare,
  faTrash,
  faEdit,
  faCheck,
  faTimes,
  faChevronDown,
  faChevronUp,
} from '@fortawesome/free-solid-svg-icons';
import { faHeart as faHeartRegular } from '@fortawesome/free-regular-svg-icons';
import {
  CommentsService,
  CommentTreeItem,
  LikesService,
  CommentLikeResponse,
} from '@mkopp/api-clients/backend';

@Component({
  selector: 'mysite-comment-item',
  standalone: true,
  imports: [CommonModule, FormsModule, FontAwesomeModule, CommentItemComponent],
  template: `
    <div class="card bg-base-100 shadow-sm">
      <div class="card-body p-4">
        <!-- Comment Header -->
        <div class="mb-2 flex items-start justify-between">
          <div class="flex items-center gap-2">
            <div class="avatar placeholder">
              <div class="w-10 rounded-full bg-primary text-primary-content">
                <span class="text-xs">{{ getUserInitials() }}</span>
              </div>
            </div>
            <div>
              <!-- ToDo: Replace with actual user name -->
              <p class="text-sm font-semibold">
                User {{ comment.userId?.substring(0, 8) }}
              </p>
              <p class="text-xs text-base-content/60">
                {{ comment.createdAt | date: 'short' }}
              </p>
            </div>
          </div>

          @if (isOwnComment()) {
            <div class="dropdown dropdown-end">
              <label tabindex="0" class="btn btn-circle btn-ghost btn-sm">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-5 w-5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z"
                  />
                </svg>
              </label>
              <ul
                tabindex="0"
                class="menu dropdown-content w-32 rounded-box bg-base-200 p-2 shadow"
              >
                <li>
                  <a (click)="startEdit()"
                    ><fa-icon [icon]="faEdit"></fa-icon> Edit</a
                  >
                </li>
                <li>
                  <a (click)="deleteComment()" class="text-error"
                    ><fa-icon [icon]="faTrash"></fa-icon> Delete</a
                  >
                </li>
              </ul>
            </div>
          }
        </div>

        <!-- Comment Content (View or Edit Mode) -->
        @if (isEditing()) {
          <div class="mb-3">
            <textarea
              class="textarea textarea-bordered w-full"
              rows="3"
              [(ngModel)]="editContent"
              [disabled]="savingEdit()"
            ></textarea>
            <div class="mt-2 flex gap-2">
              <button
                class="btn btn-primary btn-sm"
                (click)="saveEdit()"
                [disabled]="!editContent.trim() || savingEdit()"
              >
                <fa-icon [icon]="faCheck"></fa-icon>
                @if (savingEdit()) {
                  <span class="loading loading-spinner loading-xs"></span>
                }
                Save
              </button>
              <button
                class="btn btn-ghost btn-sm"
                (click)="cancelEdit()"
                [disabled]="savingEdit()"
              >
                <fa-icon [icon]="faTimes"></fa-icon>
                Cancel
              </button>
            </div>
          </div>
        } @else {
          <p class="mb-3 whitespace-pre-wrap text-sm">{{ comment.content }}</p>
        }

        <!-- Action Buttons -->
        <div class="flex items-center gap-4 text-sm">
          <!-- Like Button -->
          <button
            class="flex items-center gap-1 transition-colors hover:text-error"
            [class.text-error]="likeInfo().isLiked"
            (click)="toggleLike()"
            [disabled]="togglingLike() || !isAuthenticated"
          >
            @if (togglingLike()) {
              <span class="loading loading-spinner loading-xs"></span>
            } @else {
              <fa-icon
                [icon]="likeInfo().isLiked ? faHeartSolid : faHeartRegular"
              ></fa-icon>
            }
            <span>{{ likeInfo().count || 0 }}</span>
          </button>

          <!-- Reply Button -->
          @if (isAuthenticated) {
            <button
              class="flex items-center gap-1 transition-colors hover:text-primary"
              (click)="toggleReplyForm()"
            >
              <fa-icon [icon]="faReply"></fa-icon>
              <span>Reply</span>
            </button>
          }

          <!-- Share Button -->
          <button
            class="flex items-center gap-1 transition-colors hover:text-info"
            (click)="shareComment()"
          >
            <fa-icon [icon]="faShare"></fa-icon>
            <span>Share</span>
          </button>

          <!-- Show Replies Button -->
          @if (comment.replyCount && comment.replyCount! > 0) {
            <button
              class="flex items-center gap-1 transition-colors hover:text-primary"
              (click)="toggleReplies()"
            >
              <fa-icon
                [icon]="showReplies() ? faChevronUp : faChevronDown"
              ></fa-icon>
              <span
                >{{ comment.replyCount }}
                {{ comment.replyCount === 1 ? 'Reply' : 'Replies' }}</span
              >
            </button>
          }
        </div>

        <!-- Reply Form -->
        @if (showReplyForm()) {
          <div class="ml-8 mt-4">
            <textarea
              class="textarea textarea-bordered textarea-sm w-full"
              placeholder="Write a reply..."
              rows="2"
              [(ngModel)]="replyContent"
              [disabled]="submittingReply()"
            ></textarea>
            <div class="mt-2 flex gap-2">
              <button
                class="btn btn-primary btn-sm"
                (click)="submitReply()"
                [disabled]="!replyContent.trim() || submittingReply()"
              >
                @if (submittingReply()) {
                  <span class="loading loading-spinner loading-xs"></span>
                }
                Post Reply
              </button>
              <button
                class="btn btn-ghost btn-sm"
                (click)="cancelReply()"
                [disabled]="submittingReply()"
              >
                Cancel
              </button>
            </div>
          </div>
        }

        <!-- Replies List -->
        @if (showReplies() && replies().length > 0) {
          <div class="ml-8 mt-4 space-y-3">
            @for (reply of replies(); track reply.id) {
              <mysite-comment-item
                [comment]="reply"
                [blogPostId]="blogPostId"
                [currentUserId]="currentUserId"
                [isAuthenticated]="isAuthenticated"
                [isReply]="true"
                (commentDeleted)="handleReplyDeleted($event)"
                (commentUpdated)="handleReplyUpdated($event)"
              />
            }

            @if (hasMoreReplies()) {
              <button
                class="btn btn-outline btn-sm"
                (click)="loadMoreReplies()"
                [disabled]="loadingReplies()"
              >
                @if (loadingReplies()) {
                  <span class="loading loading-spinner loading-xs"></span>
                }
                Load More Replies
              </button>
            }
          </div>
        }
      </div>
    </div>
  `,
})
export class CommentItemComponent implements OnInit {
  private commentsService = inject(CommentsService);
  private likesService = inject(LikesService);

  @Input({ required: true }) comment!: CommentTreeItem;
  @Input({ required: true }) blogPostId!: string;
  @Input() currentUserId?: string;
  @Input() isAuthenticated = false;
  @Input() isReply = false;

  @Output() commentDeleted = new EventEmitter<{ commentId: string }>();
  @Output() commentUpdated = new EventEmitter<{ comment: CommentTreeItem }>();
  @Output() replyAdded = new EventEmitter<{ commentId: string }>();

  // UI State
  isEditing = signal(false);
  editContent = '';
  savingEdit = signal(false);

  showReplyForm = signal(false);
  replyContent = '';
  submittingReply = signal(false);

  showReplies = signal(false);
  replies = signal<CommentTreeItem[]>([]);
  repliesPage = signal(0);
  hasMoreReplies = signal(false);
  loadingReplies = signal(false);

  likeInfo = signal<CommentLikeResponse>({ count: 0, isLiked: false });
  togglingLike = signal(false);

  // Icons
  faHeartSolid = faHeartSolid;
  faHeartRegular = faHeartRegular;
  faReply = faReply;
  faShare = faShare;
  faTrash = faTrash;
  faEdit = faEdit;
  faCheck = faCheck;
  faTimes = faTimes;
  faChevronDown = faChevronDown;
  faChevronUp = faChevronUp;

  ngOnInit() {
    // Initialize like info from comment data
    this.likeInfo.set({
      count: this.comment.likeCount || 0,
      isLiked: false,
    });

    // Load actual like status if authenticated
    if (this.isAuthenticated) {
      this.loadLikeInfo();
    }
  }

  isOwnComment(): boolean {
    return this.currentUserId === this.comment.userId;
  }

  getUserInitials(): string {
    const userId = this.comment.userId || '';
    return userId.substring(0, 2).toUpperCase();
  }

  // Edit functionality
  startEdit() {
    this.editContent = this.comment.content || '';
    this.isEditing.set(true);
  }

  cancelEdit() {
    this.isEditing.set(false);
    this.editContent = '';
  }

  saveEdit() {
    if (!this.editContent.trim() || !this.comment.id) return;

    this.savingEdit.set(true);
    this.commentsService
      .updateComment(this.comment.id!, {
        content: this.editContent,
      })
      .subscribe({
        next: (updated) => {
          this.comment = updated;
          this.commentUpdated.emit({ comment: updated });
          this.isEditing.set(false);
          this.savingEdit.set(false);
        },
        error: (err) => {
          console.error('Failed to update comment:', err);
          this.savingEdit.set(false);
        },
      });
  }

  deleteComment() {
    if (!confirm('Are you sure you want to delete this comment?')) return;

    if (!this.comment.id) return;

    this.commentsService.deleteComment(this.comment.id!).subscribe({
      next: () => {
        this.commentDeleted.emit({ commentId: this.comment.id! });
      },
      error: (err) => {
        console.error('Failed to delete comment:', err);
      },
    });
  }

  // Reply functionality
  toggleReplyForm() {
    this.showReplyForm.update((v) => !v);
    if (!this.showReplyForm()) {
      this.replyContent = '';
    }
  }

  cancelReply() {
    this.showReplyForm.set(false);
    this.replyContent = '';
  }

  submitReply() {
    if (!this.replyContent.trim()) return;

    this.submittingReply.set(true);

    this.commentsService
      .createComment(this.blogPostId, {
        content: this.replyContent,
        parentCommentId: this.comment.id,
      })
      .subscribe({
        next: (newReply) => {
          this.replies.update((existing) => [newReply, ...existing]);
          this.comment = {
            ...this.comment,
            replyCount: (this.comment.replyCount || 0) + 1,
          };
          this.replyAdded.emit({ commentId: this.comment.id! });
          this.replyContent = '';
          this.showReplyForm.set(false);
          this.submittingReply.set(false);

          // Auto-expand replies
          if (!this.showReplies()) {
            this.showReplies.set(true);
          }
        },
        error: (err) => {
          console.error('Failed to post reply:', err);
          this.submittingReply.set(false);
        },
      });
  }

  // Replies loading
  toggleReplies() {
    this.showReplies.update((v) => !v);
    if (this.showReplies() && this.replies().length === 0) {
      this.loadReplies();
    }
  }

  loadReplies() {
    if (!this.comment.id) return;

    this.loadingReplies.set(true);

    this.commentsService
      .getReplies(this.comment.id!, this.repliesPage(), 5, ['createdAt,asc'])
      .subscribe({
        next: (response) => {
          this.replies.set(response.content || []);
          this.hasMoreReplies.set(!response.last);
          this.loadingReplies.set(false);
        },
        error: (err) => {
          console.error('Failed to load replies:', err);
          this.loadingReplies.set(false);
        },
      });
  }

  loadMoreReplies() {
    if (!this.comment.id) return;

    this.loadingReplies.set(true);
    this.repliesPage.update((p) => p + 1);

    this.commentsService
      .getReplies(this.comment.id!, this.repliesPage(), 5, ['createdAt,asc'])
      .subscribe({
        next: (response) => {
          this.replies.update((existing) => [
            ...existing,
            ...(response.content || []),
          ]);
          this.hasMoreReplies.set(!response.last);
          this.loadingReplies.set(false);
        },
        error: (err) => {
          console.error('Failed to load more replies:', err);
          this.loadingReplies.set(false);
          this.repliesPage.update((p) => p - 1);
        },
      });
  }

  handleReplyDeleted(event: { commentId: string }) {
    this.replies.update((existing) =>
      existing.filter((r) => r.id !== event.commentId)
    );
    this.comment = {
      ...this.comment,
      replyCount: Math.max(0, (this.comment.replyCount || 0) - 1),
    };
  }

  handleReplyUpdated(event: { comment: CommentTreeItem }) {
    this.replies.update((existing) =>
      existing.map((r) => (r.id === event.comment.id ? event.comment : r))
    );
  }

  // Like functionality
  loadLikeInfo() {
    if (!this.comment.id) return;

    this.commentsService.getCommentLikeInfo(this.comment.id!).subscribe({
      next: (info) => {
        this.likeInfo.set(info);
      },
      error: (err) => {
        console.error('Failed to load like info:', err);
      },
    });
  }

  toggleLike() {
    if (!this.isAuthenticated || !this.comment.id) return;

    this.togglingLike.set(true);

    this.commentsService.toggleCommentLike(this.comment.id!).subscribe({
      next: (response) => {
        this.likeInfo.set(response);
        this.togglingLike.set(false);
      },
      error: (err) => {
        console.error('Failed to toggle like:', err);
        this.togglingLike.set(false);
      },
    });
  }

  // Share functionality
  async shareComment() {
    const url = `${window.location.origin}${window.location.pathname}#comment-${this.comment.id}`;

    if (navigator.share) {
      try {
        await navigator.share({
          title: 'Comment',
          text: this.comment.content?.substring(0, 100),
          url: url,
        });
      } catch (err) {
        if (err instanceof Error && err.name !== 'AbortError') {
          this.copyToClipboard(url);
        }
      }
    } else {
      this.copyToClipboard(url);
    }
  }

  private copyToClipboard(text: string) {
    navigator.clipboard
      .writeText(text)
      .then(() => {
        alert('Link copied to clipboard!');
      })
      .catch((err) => {
        console.error('Failed to copy:', err);
      });
  }
}
