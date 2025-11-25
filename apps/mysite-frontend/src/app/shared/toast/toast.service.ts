import { Injectable, signal } from '@angular/core';
import { ToastInfo, ToastType } from './toast-info.model';

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  toasts = signal<ToastInfo[]>([]);

  show(body: string, type: ToastType, autoHideDuration = 2000) {
    const classMap: Record<ToastType, string> = {
      SUCCESS: 'alert alert-success',
      INFO: 'alert alert-info',
      DANGER: 'alert alert-error',
      WARNING: 'alert alert-warning',
    };

    const toastInfo: ToastInfo = {
      body,
      type,
      className: classMap[type],
    };
    this.toasts.update((toasts) => [...toasts, toastInfo]);

    if (autoHideDuration > 0) {
      setTimeout(() => this.remove(toastInfo), autoHideDuration);
    }
  }

  remove(toast: ToastInfo): void {
    this.toasts.update((toasts) =>
      toasts.filter((toastToCompare) => toastToCompare != toast)
    );
  }
}
