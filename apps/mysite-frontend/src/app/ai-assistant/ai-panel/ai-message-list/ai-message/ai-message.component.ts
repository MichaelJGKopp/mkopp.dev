import {
  Component,
  Input,
  PLATFORM_ID,
  inject,
  afterNextRender,
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { AiChatMessageResponseDto, Message } from '@mkopp/api-clients/backend';
import { MarkdownModule } from 'ngx-markdown';

@Component({
  selector: 'mysite-ai-message',
  standalone: true,
  imports: [CommonModule, MarkdownModule],
  templateUrl: './ai-message.component.html',
})
export class AiMessageComponent {
  private platformId = inject(PLATFORM_ID);

  @Input() message!: AiChatMessageResponseDto;

  constructor() {
    afterNextRender(() => {
      this.onMarkdownReady();
    });
  }

  get isUser(): boolean {
    return this.message.messageType === 'USER';
  }

  get messageText(): string {
    return typeof this.message?.text === 'string' ? this.message.text : '';
  }

  onMarkdownReady() {
    if (isPlatformBrowser(this.platformId)) {
      setTimeout(() => {
        if ((window as any).hljs) {
          (window as any).hljs.highlightAll();
        }
      }, 100);
    }
  }
}
