import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'mysite-ai-close-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button
      type="button"
      (click)="aiClose.emit()"
      class="btn btn-circle btn-ghost btn-xs"
      [attr.aria-label]="'Close'"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-4 w-4"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M6 18L18 6M6 6l12 12"
        />
      </svg>
    </button>
  `,
})
export class AiCloseButtonComponent {
  @Output() aiClose = new EventEmitter<void>();
}
