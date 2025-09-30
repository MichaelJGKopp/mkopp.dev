import { HttpErrorResponse } from '@angular/common/http';

export type StatusNotification = 'OK' | 'ERROR' | 'INIT';

export class State<T, V = HttpErrorResponse> {
  constructor(
    public status: StatusNotification,
    public value?: T,
    public error?: V | HttpErrorResponse
  ) {}

  // factory methods
  
  static forSuccess<T, V = HttpErrorResponse>(value: T): State<T, V> {
    return new State<T, V>('OK', value);
  }

  static forSuccessEmpty<T, V = HttpErrorResponse>(): State<T, V> {
    return new State<T, V>('OK');
  }

  static forError<T, V = HttpErrorResponse>(
    error: V | HttpErrorResponse = new HttpErrorResponse({
      error: 'Unknown Error',
    }),
    value?: T
  ): State<T, V> {
    return new State<T, V>('ERROR', value, error);
  }

  static forInit<T, V = HttpErrorResponse>(): State<T, V> {
    return new State<T, V>('INIT');
  }
}
