import {
  Component,
  Input,
  Output,
  EventEmitter,
  signal,
  PLATFORM_ID,
  inject,
  AfterViewInit,
  OnDestroy,
  ElementRef,
  ViewChild,
  effect,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { AiHeaderComponent } from './ai-header/ai-header.component';
import { AiMessageListComponent } from './ai-message-list/ai-message-list.component';
import { AiQuickPromptsComponent } from './ai-quick-prompts/ai-quick-prompts.component';
import { AiInputComponent } from './ai-input/ai-input.component';
import { AiService } from '../ai.service';
import { getPanelWidth, savePanelWidth } from '../conversation-id.util';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'mysite-ai-panel',
  standalone: true,
  imports: [
    CommonModule,
    AiHeaderComponent,
    AiMessageListComponent,
    AiQuickPromptsComponent,
    AiInputComponent,
  ],
  templateUrl: './ai-panel.component.html',
  styleUrls: ['./ai-panel.component.css'],
})
export class AiPanelComponent implements AfterViewInit, OnDestroy {
  @Input() conversationId = '';
  @Output() closePanel = new EventEmitter<void>();
  @ViewChild('resizeHandle') resizeHandle?: ElementRef<HTMLDivElement>;
  @ViewChild('messageContainer') messageContainer?: ElementRef<HTMLDivElement>;

  private platformId = inject(PLATFORM_ID);
  aiService = inject(AiService);

  panelWidth = signal<number>(380);
  isResizing = signal<boolean>(false);
  isMaximized = signal<boolean>(false);
  isHeaderVisible = signal<boolean>(true);
  private savedPanelWidth = 380;
  private lastScrollTop = 0;

  constructor() {
    // Manage body overflow when maximized
    effect(() => {
      if (!isPlatformBrowser(this.platformId)) return;

      if (this.isMaximized()) {
        document.body.style.overflow = 'hidden';
      } else {
        document.body.style.overflow = '';
      }
    });
  }

  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.panelWidth.set(getPanelWidth(this.platformId));
      this.savedPanelWidth = this.panelWidth();
      this.setupResizeListener();

      const isMobile = window.innerWidth < 768;
      if (isMobile) {
        this.isMaximized.set(true);
      }
    }
  }

  onClose(): void {
    this.closePanel.emit();
  }

  toggleMaximize(): void {
    if (this.isMaximized()) {
      // Restore to saved width
      this.panelWidth.set(this.savedPanelWidth);
      this.isMaximized.set(false);
    } else {
      // Save current width and maximize
      this.savedPanelWidth = this.panelWidth();
      this.isMaximized.set(true);
    }
  }

  clearConversation(): void {
    this.aiService.clearHistory();
    // Create and set new conversation ID
    if (isPlatformBrowser(this.platformId)) {
      const newId = crypto.randomUUID();
      localStorage.setItem('mkopp-ai-conversation-id', newId);
      this.conversationId = newId;
      this.aiService.conversationId.set(newId);
    }
  }

  sendMessage(message: string): void {
    this.aiService.sendMessage(message, this.conversationId).subscribe();
  }

  onScroll(event: Event): void {
    if (!isPlatformBrowser(this.platformId)) return;

    // Get the actual scroll container from the message list component
    const messageList = document.querySelector(
      'mysite-ai-message-list .h-full.overflow-y-auto'
    );
    if (!messageList) return;

    const scrollTop = messageList.scrollTop;

    // Only hide header on mobile (screen width < 768px)
    if (window.innerWidth >= 768) {
      this.isHeaderVisible.set(true);
      return;
    }

    // Show header when scrolling up, hide when scrolling down
    if (scrollTop > this.lastScrollTop && scrollTop > 50) {
      // Scrolling down & past threshold
      this.isHeaderVisible.set(false);
    } else {
      // Scrolling up or at top
      this.isHeaderVisible.set(true);
    }

    this.lastScrollTop = scrollTop <= 0 ? 0 : scrollTop;
  }

  private setupResizeListener(): void {
    if (!isPlatformBrowser(this.platformId) || !this.resizeHandle) return;

    const handle = this.resizeHandle.nativeElement;
    let startX = 0;
    let startWidth = 0;

    const onMouseDown = (e: MouseEvent) => {
      startX = e.clientX;
      startWidth = this.panelWidth();
      this.isResizing.set(true);
      document.addEventListener('mousemove', onMouseMove);
      document.addEventListener('mouseup', onMouseUp);
      e.preventDefault();
    };

    const onMouseMove = (e: MouseEvent) => {
      if (!this.isResizing()) return;
      const deltaX = startX - e.clientX;
      const newWidth = Math.max(
        280,
        Math.min(window.innerWidth * 0.6, startWidth + deltaX)
      );
      this.panelWidth.set(newWidth);
    };

    const onMouseUp = () => {
      this.isResizing.set(false);
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onMouseUp);
      savePanelWidth(this.panelWidth(), this.platformId);
    };

    handle.addEventListener('mousedown', onMouseDown);
  }

  ngOnDestroy(): void {
    if (isPlatformBrowser(this.platformId)) {
      document.body.style.overflow = '';
    }
  }
}
