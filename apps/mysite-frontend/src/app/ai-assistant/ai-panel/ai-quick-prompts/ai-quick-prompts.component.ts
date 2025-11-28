import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'mysite-ai-quick-prompts',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ai-quick-prompts.component.html',
})
export class AiQuickPromptsComponent {
  @Output() promptSelect = new EventEmitter<string>();

  quickPrompts = [
    {
      label: '👋 Introduction',
      text: 'Tell me about Michael Kopp and his experience',
    },
    {
      label: '💼 Recent Projects',
      text: 'What are some recent projects Michael has worked on?',
    },
    {
      label: '🛠️ Tech Stack',
      text: 'What technologies does Michael work with?',
    },
    { label: '📚 Blog Posts', text: 'Show me recent blog posts' },
    { label: '📧 Contact', text: 'How can I contact Michael?' },
  ];

  selectPrompt(text: string): void {
    this.promptSelect.emit(text);
  }
}
