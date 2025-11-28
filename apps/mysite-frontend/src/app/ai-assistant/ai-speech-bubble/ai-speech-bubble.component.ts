import {
  Component,
  Output,
  EventEmitter,
  afterNextRender,
  PLATFORM_ID,
  inject,
  signal,
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { AiCloseButtonComponent } from '../ai-close-button/ai-close-button.component';

const SPEECH_BUBBLE_DISMISSED_KEY = 'mkopp-ai-speech-bubble-dismissed';

@Component({
  selector: 'mysite-ai-speech-bubble',
  standalone: true,
  imports: [CommonModule, AiCloseButtonComponent],
  templateUrl: './ai-speech-bubble.component.html',
  styleUrls: ['./ai-speech-bubble.component.css'],
})
export class AiSpeechBubbleComponent {
  private platformId = inject(PLATFORM_ID);

  @Output() dismiss = new EventEmitter<void>();

  isVisible = signal(false);

  constructor() {
    afterNextRender(() => {
      if (isPlatformBrowser(this.platformId)) {
        // Check if user has already dismissed the bubble
        const dismissed = localStorage.getItem(SPEECH_BUBBLE_DISMISSED_KEY);
        this.isVisible.set(!dismissed);
      }
    });
  }

  onClose(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(SPEECH_BUBBLE_DISMISSED_KEY, 'true');
    }
    this.isVisible.set(false);
    this.dismiss.emit();
  }
}
