import { Injectable, signal, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import {
  AiChatControllerService,
  AiChatMessageResponseDto,
  Message,
} from '@mkopp/api-clients/backend';
import { Observable, tap, catchError, of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AiService {
  private platformId = inject(PLATFORM_ID);
  private aiChatService = inject(AiChatControllerService);

  messages = signal<AiChatMessageResponseDto[]>([]);
  loading = signal<boolean>(false);
  error = signal<string | null>(null);
  conversationId = signal<string>('');
  isOpen = signal<boolean>(false);

  loadHistory(conversationId: string): void {
    if (!conversationId) return;

    this.aiChatService.history(conversationId).subscribe({
      next: (history) => {
        // Ensure history is an array
        this.messages.set(history || []);
      },
      error: (err) => {
        console.error('Failed to load history:', err);
        this.messages.set([]); // Set empty array on error
      },
    });
  }

  sendMessage(message: string, conversationId: string): Observable<string> {
    if (!conversationId) {
      console.error('No conversation ID available');
      return of('');
    }

    this.error.set(null);
    this.loading.set(true);

    // Add user message immediately
    const userMessage: Message = {
      messageType: 'USER',
      text: message,
    };
    this.messages.update((msgs) => [...msgs, userMessage]);

    return this.aiChatService.chat(message, conversationId).pipe(
      tap((response) => {
        // Add assistant message
        const assistantMessage: Message = {
          messageType: 'ASSISTANT',
          text: response,
        };
        this.messages.update((msgs) => [...msgs, assistantMessage]);
        console.log('AI response:', response);
        this.saveToLocalStorage();
        this.loading.set(false);
      }),
      catchError((err) => {
        console.error('Chat error:', err);
        this.error.set('Failed to get response. Please try again.');

        // Add error message
        const errorMessage: Message = {
          messageType: 'ASSISTANT',
          text: 'Sorry, I encountered an error. Please try again.',
        };
        this.messages.update((msgs) => [...msgs, errorMessage]);
        this.loading.set(false);
        return of('');
      })
    );
  }

  clearHistory(): void {
    this.messages.set([]);
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('mkopp-ai-chat-history');
    }
  }

  togglePanel(): void {
    this.isOpen.update((open) => !open);
  }

  openPanel(): void {
    this.isOpen.set(true);
  }

  closePanel(): void {
    this.isOpen.set(false);
  }

  private saveToLocalStorage(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(
        'mkopp-ai-chat-history',
        JSON.stringify(this.messages())
      );
    }
  }
}
