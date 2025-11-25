import { Component, inject } from '@angular/core';
import { ToastService } from './toast.service';

@Component({
  selector: 'mysite-toast',
  templateUrl: './toast.component.html',
})
export class ToastComponent {
  protected toastService = inject(ToastService);
}
