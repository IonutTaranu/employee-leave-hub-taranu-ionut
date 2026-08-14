export type Role = 'EMPLOYEE' | 'MANAGER' | 'ADMIN';
export type LeaveStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface UserSummary { id: number; name: string; email: string; role: Role; departmentId: number; departmentName: string; annualLeaveDays: number; availableLeaveDays: number; }
export interface AuthResponse { token: string; user: UserSummary; }
export interface Department { id: number; name: string; managerId: number | null; managerName: string | null; maxAbsentEmployees: number; employeeCount: number; }
export interface Employee { id: number; name: string; email: string; role: Role; departmentId: number; departmentName: string; annualLeaveDays: number; availableLeaveDays: number; active: boolean; }
export interface LeaveType { id: number; name: string; code: string; requiresAttachment: boolean; paid: boolean; }
export interface Attachment { id: number; fileName: string; contentType: string; fileSize: number; uploadedAt: string; }
export interface WorkflowEntry { id: number; oldStatus: LeaveStatus | null; currentStatus: LeaveStatus; changedById: number; changedByName: string; changedAt: string; comment: string | null; }
export interface LeaveRequest { id: number; employeeId: number; employeeName: string; departmentId: number; departmentName: string; leaveTypeId: number; leaveTypeName: string; leaveTypeCode: string; attachmentRequired: boolean; startDate: string; endDate: string; workingDays: number; status: LeaveStatus; reason: string | null; createdAt: string; updatedAt: string; attachments: Attachment[]; workflow: WorkflowEntry[]; }
export interface Dashboard { balance: { available: number; consumed: number; pending: number; annual: number }; totalRequests: number; pendingRequests: number; approvedRequests: number; rejectedRequests: number; employees: number; departments: number; recentRequests: LeaveRequest[]; }
export interface CalendarEvent { requestId: number; employeeName: string; departmentName: string; leaveTypeCode: string; startDate: string; endDate: string; status: LeaveStatus; overlapWarning: boolean; }
export interface DepartmentStatistic { departmentId: number; departmentName: string; employees: number; requests: number; approvedDays: number; pendingRequests: number; }
export interface ReportSummary { totalRequests: number; pendingRequests: number; approvedRequests: number; rejectedRequests: number; cancelledRequests: number; departments: DepartmentStatistic[]; }
export interface LeaveRequestInput { leaveTypeId: number; startDate: string; endDate: string; reason: string; }
