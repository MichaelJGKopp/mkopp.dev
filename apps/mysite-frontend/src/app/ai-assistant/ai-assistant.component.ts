import {
  Component,
  signal,
  PLATFORM_ID,
  inject,
  afterNextRender,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { AiToggleButtonComponent } from './ai-toggle-button/ai-toggle-button.component';
import { AiPanelComponent } from './ai-panel/ai-panel.component';
import { AiService } from './ai.service';
import { loadOrCreateConversationId } from './conversation-id.util';

@Component({
  selector: 'mysite-ai-assistant',
  standalone: true,
  imports: [CommonModule, AiToggleButtonComponent, AiPanelComponent],
  templateUrl: './ai-assistant.component.html',
})
export class AiAssistantComponent {
  private platformId = inject(PLATFORM_ID);
  private aiService = inject(AiService);

  isOpen = signal<boolean>(false);
  conversationId = signal<string>('');

  constructor() {
    afterNextRender(() => {
      const id = loadOrCreateConversationId(this.platformId);
      this.conversationId.set(id);
      this.aiService.conversationId.set(id);
      this.aiService.loadHistory(id);
    });
  }

  togglePanel(): void {
    this.isOpen.update((open) => !open);
  }

  closePanel(): void {
    this.isOpen.set(false);
  }
}
