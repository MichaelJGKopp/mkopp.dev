import { isPlatformBrowser } from '@angular/common';

const CONVERSATION_ID_KEY = 'mkopp-ai-conversation-id';

export function loadOrCreateConversationId(platformId: object): string {
  if (!isPlatformBrowser(platformId)) {
    return '';
  }

  const stored = localStorage.getItem(CONVERSATION_ID_KEY);
  if (stored) {
    return stored;
  }

  const newId = crypto.randomUUID();
  localStorage.setItem(CONVERSATION_ID_KEY, newId);
  return newId;
}

export function getPanelWidth(platformId: object): number {
  if (!isPlatformBrowser(platformId)) {
    return 380;
  }

  const stored = localStorage.getItem('mkopp-ai-panel-width');
  return stored ? parseInt(stored, 10) : 380;
}

export function savePanelWidth(width: number, platformId: object): void {
  if (isPlatformBrowser(platformId)) {
    localStorage.setItem('mkopp-ai-panel-width', width.toString());
  }
}
