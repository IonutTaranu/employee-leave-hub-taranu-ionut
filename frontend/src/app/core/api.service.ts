import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Attachment, CalendarEvent, Dashboard, Department, Employee, LeaveRequest, LeaveRequestInput, LeaveStatus, LeaveType, ReportSummary, Role } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  dashboard() { return this.http.get<Dashboard>('/api/dashboard'); }
  departments() { return this.http.get<Department[]>('/api/departments'); }
  leaveTypes() { return this.http.get<LeaveType[]>('/api/leave-types'); }
  employees(departmentId?: number) { const params = departmentId ? new HttpParams().set('departmentId', departmentId) : undefined; return this.http.get<Employee[]>('/api/employees', { params }); }
  requests(filters: { status?: LeaveStatus | ''; departmentId?: number | null; leaveTypeId?: number | null; employee?: string; from?: string; to?: string } = {}) {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, value]) => { if (value !== undefined && value !== null && value !== '') params = params.set(key, String(value)); });
    return this.http.get<LeaveRequest[]>('/api/leave-requests', { params });
  }
  request(id: number) { return this.http.get<LeaveRequest>(`/api/leave-requests/${id}`); }
  createRequest(input: LeaveRequestInput) { return this.http.post<LeaveRequest>('/api/leave-requests', input); }
  updateRequest(id: number, input: LeaveRequestInput) { return this.http.put<LeaveRequest>(`/api/leave-requests/${id}`, input); }
  submitRequest(id: number) { return this.http.post<LeaveRequest>(`/api/leave-requests/${id}/submit`, {}); }
  cancelRequest(id: number) { return this.http.post<LeaveRequest>(`/api/leave-requests/${id}/cancel`, {}); }
  decideRequest(id: number, decision: 'APPROVED' | 'REJECTED', comment: string) { return this.http.post<LeaveRequest>(`/api/leave-requests/${id}/decision`, { decision, comment }); }
  deleteRequest(id: number) { return this.http.delete<void>(`/api/leave-requests/${id}`); }
  uploadAttachment(id: number, file: File) { const data = new FormData(); data.append('file', file); return this.http.post<Attachment>(`/api/leave-requests/${id}/attachments`, data); }
  deleteAttachment(id: number) { return this.http.delete<void>(`/api/attachments/${id}`); }
  downloadAttachment(id: number) { return this.http.get(`/api/attachments/${id}`, { observe: 'response', responseType: 'blob' }); }
  calendar(departmentId: number | null, from: string, to: string) { let params = new HttpParams().set('from', from).set('to', to); if (departmentId) params = params.set('departmentId', departmentId); return this.http.get<CalendarEvent[]>('/api/calendar', { params }); }
  createEmployee(input: { name: string; email: string; password: string; role: Role; departmentId: number; annualLeaveDays: number; availableLeaveDays: number; active: boolean }) { return this.http.post<Employee>('/api/employees', input); }
  updateEmployee(id: number, input: { name: string; email: string; password: string; role: Role; departmentId: number; annualLeaveDays: number; availableLeaveDays: number; active: boolean }) { return this.http.put<Employee>(`/api/employees/${id}`, input); }
  deleteEmployee(id: number) { return this.http.delete<void>(`/api/employees/${id}`); }
  createDepartment(input: { name: string; managerId: number | null; maxAbsentEmployees: number }) { return this.http.post<Department>('/api/departments', input); }
  updateDepartment(id: number, input: { name: string; managerId: number | null; maxAbsentEmployees: number }) { return this.http.put<Department>(`/api/departments/${id}`, input); }
  deleteDepartment(id: number) { return this.http.delete<void>(`/api/departments/${id}`); }
  createLeaveType(input: { name: string; code: string; requiresAttachment: boolean; paid: boolean }) { return this.http.post<LeaveType>('/api/leave-types', input); }
  updateLeaveType(id: number, input: { name: string; code: string; requiresAttachment: boolean; paid: boolean }) { return this.http.put<LeaveType>(`/api/leave-types/${id}`, input); }
  deleteLeaveType(id: number) { return this.http.delete<void>(`/api/leave-types/${id}`); }
  reportSummary() { return this.http.get<ReportSummary>('/api/reports/summary'); }
  downloadPdf(path: string) { return this.http.get(`/api${path}`, { observe: 'response', responseType: 'blob' }); }
}
