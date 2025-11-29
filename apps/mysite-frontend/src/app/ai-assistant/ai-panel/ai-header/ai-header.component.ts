import { Component, Output, EventEmitter, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'mysite-ai-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ai-header.component.html',
})
export class AiHeaderComponent {
  @Input() isMaximized = false;
  @Input() isHeaderVisible = true;
  @Output() aiHeaderClose = new EventEmitter<void>();
  @Output() clear = new EventEmitter<void>();
  @Output() maximize = new EventEmitter<void>();

  onClose(): void {
    this.aiHeaderClose.emit();
  }

  onClear(): void {
    if (confirm('Are you sure you want to clear this conversation?')) {
      this.clear.emit();
    }
  }

  onMaximize(): void {
    this.maximize.emit();
  }
}
