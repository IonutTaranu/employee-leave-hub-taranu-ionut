import { HttpErrorResponse } from '@angular/common/http';

export function errorMessage(error: unknown, fallback = 'Operatia nu a putut fi finalizata.'): string {
  if (error instanceof HttpErrorResponse) {
    return error.error?.detail || error.error?.message || fallback;
  }
  return fallback;
}

export function saveBlob(blob: Blob, fileName: string): void {
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url; anchor.download = fileName; anchor.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
