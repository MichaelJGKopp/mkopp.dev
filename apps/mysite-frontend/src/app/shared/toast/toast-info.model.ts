export interface ToastInfo {
  body: string;
  type: ToastType;
  className: string;
}

export type ToastType = "SUCCESS" | "INFO" | "DANGER" | "WARNING";