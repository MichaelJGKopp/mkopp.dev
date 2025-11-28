import {
  Component,
  Input,
  Output,
  EventEmitter,
  ElementRef,
  ViewChild,
  AfterViewChecked,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { AiMessageComponent } from './ai-message/ai-message.component';
import { AiTypingIndicatorComponent } from './ai-typing-indicator/ai-typing-indicator.component';
import { AiChatMessageResponseDto, Message } from '@mkopp/api-clients/backend';

@Component({
  selector: 'mysite-ai-message-list',
  standalone: true,
  imports: [CommonModule, AiMessageComponent, AiTypingIndicatorComponent],
  templateUrl: './ai-message-list.component.html',
})
export class AiMessageListComponent implements AfterViewChecked {
  @Input() messages: AiChatMessageResponseDto[] = [];
  @Input() loading = false;
  @Output() scrollEvent = new EventEmitter<Event>();
  @ViewChild('scrollContainer') scrollContainer?: ElementRef<HTMLDivElement>;

  private shouldAutoScroll = true;
  private previousMessageCount = 0;

  get safeMessages(): AiChatMessageResponseDto[] {
    return Array.isArray(this.messages) ? this.messages : [];
  }

  ngAfterViewChecked(): void {
    const currentMessageCount = this.safeMessages.length;

    // Only auto-scroll when new messages are added and shouldAutoScroll is true
    if (
      this.shouldAutoScroll &&
      currentMessageCount > this.previousMessageCount
    ) {
      this.scrollToBottom();
    }

    this.previousMessageCount = currentMessageCount;
  }

  onScroll(): void {
    if (!this.scrollContainer) return;
    const element = this.scrollContainer.nativeElement;
    const threshold = 150;
    const atBottom =
      element.scrollHeight - element.scrollTop - element.clientHeight <
      threshold;
    this.shouldAutoScroll = atBottom;

    // Emit scroll event for parent to handle header visibility
    this.scrollEvent.emit(new Event('scroll', { bubbles: true }));
  }

  private scrollToBottom(): void {
    if (!this.scrollContainer) return;
    const element = this.scrollContainer.nativeElement;
    element.scrollTop = element.scrollHeight;
  }
}
