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
import { Oauth2AuthService } from '../../../auth/oauth2-auth.service';

@Component({
  selector: 'mysite-comment-item',
  standalone: true,
  imports: [CommonModule, FormsModule, FontAwesomeModule],
  templateUrl: './comment-item.component.html',
})
export class CommentItemComponent implements OnInit {
  private commentsService = inject(CommentsService);
  private likesService = inject(LikesService);
  private authService = inject(Oauth2AuthService);

  @Input({ required: true }) comment!: CommentTreeItem;
  @Input({ required: true }) blogPostId!: string;
  @Input() isReply = false;

  @Output() commentDeleted = new EventEmitter<{ commentId: string }>();
  @Output() commentUpdated = new EventEmitter<{ comment: CommentTreeItem }>();
  @Output() replyAdded = new EventEmitter<{ commentId: string }>();

  currentUser = this.authService.connectedUser;
  isAuthenticated = this.authService.isAuthenticated;

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
    if (this.isAuthenticated()) {
      this.loadLikeInfo();
    }
  }

  isOwnComment(): boolean {
    return this.currentUser()?.id === this.comment.userId;
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
    if (!this.isAuthenticated() || !this.comment.id) return;

    this.togglingLike.set(true);

    console.log('Current like info before toggle:', this.likeInfo());

    this.commentsService.toggleCommentLike(this.comment.id!).subscribe({
      next: (response) => {
        console.log('Toggle like response:', response);
        this.likeInfo.set(response);
        console.log('Like info after set:', this.likeInfo());
        this.togglingLike.set(false);
      },
      error: (err) => {
        console.error('Failed to toggle like:', err);
        this.togglingLike.set(false);
      },
    });
  }

  login() {
    this.authService.login();
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
