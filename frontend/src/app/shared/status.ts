import { LeaveStatus } from '../core/models';

export const STATUS_LABELS: Record<LeaveStatus, string> = {
  DRAFT: 'Ciorna', PENDING: 'In asteptare', APPROVED: 'Aprobata', REJECTED: 'Respinsa', CANCELLED: 'Anulata',
};
export function formatDate(value: string): string { return new Intl.DateTimeFormat('ro-RO', { day: '2-digit', month: 'short', year: 'numeric' }).format(new Date(`${value}T12:00:00`)); }
