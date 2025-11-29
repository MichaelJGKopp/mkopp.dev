import {
  Component,
  Output,
  EventEmitter,
  Input,
  signal,
  ViewChild,
  ElementRef,
  AfterViewInit,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface AttachedFile {
  name: string;
  content: string;
  type: 'file' | 'url';
}

@Component({
  selector: 'mysite-ai-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-input.component.html',
})
export class AiInputComponent implements AfterViewInit {
  @Input() disabled = false;
  @Output() sendMessage = new EventEmitter<string>();
  @ViewChild('messageInput') messageInput?: ElementRef<HTMLTextAreaElement>;

  message = signal<string>('');
  attachedFiles = signal<AttachedFile[]>([]);
  selectedModel = signal<string>('gemini');
  selectedTool = signal<string | null>(null);

  models = [
    { value: 'gemini', label: 'Google Gemini' },
    { value: 'gpt-4', label: 'GPT-4' },
    { value: 'claude', label: 'Claude' },
  ];

  tools = [
    { value: 'web-search', label: 'Web Search' },
    { value: 'code-interpreter', label: 'Code Interpreter' },
    { value: 'file-analyzer', label: 'File Analyzer' },
  ];

  get selectedModelLabel(): string {
    return (
      this.models.find((m) => m.value === this.selectedModel())?.label ||
      'Model'
    );
  }

  get selectedToolLabel(): string {
    if (!this.selectedTool()) return 'Tools';
    return (
      this.tools.find((t) => t.value === this.selectedTool())?.label || 'Tools'
    );
  }

  ngAfterViewInit(): void {
    this.adjustTextareaHeight();
  }

  adjustTextareaHeight(): void {
    if (!this.messageInput) return;
    const textarea = this.messageInput.nativeElement;

    // Reset height to auto to get the correct scrollHeight
    textarea.style.height = 'auto';

    // Calculate the number of rows based on scrollHeight
    const lineHeight = 28; // Approximate line height in pixels
    const maxRows = 12;
    const minHeight = lineHeight * 1; // 1 row minimum
    const maxHeight = lineHeight * maxRows;

    const newHeight = Math.min(
      Math.max(textarea.scrollHeight, minHeight),
      maxHeight
    );
    textarea.style.height = `${newHeight}px`;
  }

  onSend(): void {
    const msg = this.message().trim();
    if (msg && !this.disabled) {
      this.sendMessage.emit(msg);
      this.message.set('');
      // Reset textarea height after sending
      setTimeout(() => this.adjustTextareaHeight(), 0);
    }
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.onSend();
    }
  }

  onFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      const reader = new FileReader();
      reader.onload = (e) => {
        const content = e.target?.result as string;
        this.attachedFiles.update((files) => [
          ...files,
          { name: file.name, content, type: 'file' },
        ]);
      };
      reader.readAsText(file);
    }
  }

  addCurrentUrl(): void {
    const url = window.location.href;
    const urlName = window.location.pathname || 'Current Page';
    this.attachedFiles.update((files) => [
      ...files,
      { name: urlName, content: url, type: 'url' },
    ]);
  }

  removeFile(index: number): void {
    this.attachedFiles.update((files) => files.filter((_, i) => i !== index));
  }
}
