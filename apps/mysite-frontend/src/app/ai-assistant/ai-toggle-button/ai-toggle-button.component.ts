import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AiSpeechBubbleComponent } from '../ai-speech-bubble/ai-speech-bubble.component';

@Component({
  selector: 'mysite-ai-toggle-button',
  standalone: true,
  imports: [CommonModule, AiSpeechBubbleComponent],
  templateUrl: './ai-toggle-button.component.html',
})
export class AiToggleButtonComponent {
  @Input() isOpen = false;
  @Output() toggleClick = new EventEmitter<void>();

  onToggle(): void {
    this.toggleClick.emit();
  }
}
