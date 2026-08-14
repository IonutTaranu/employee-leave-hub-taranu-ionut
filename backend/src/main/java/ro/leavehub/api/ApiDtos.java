package ro.leavehub.api;

import jakarta.validation.constraints.*;
import ro.leavehub.model.LeaveStatus;
import ro.leavehub.model.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record AuthResponse(String token, UserSummary user) {
    }

    public record UserSummary(
            Long id,
            String name,
            String email,
            Role role,
            Long departmentId,
            String departmentName,
            int annualLeaveDays,
            int availableLeaveDays) {
    }

    public record DepartmentDto(
            Long id,
            String name,
            Long managerId,
            String managerName,
            int maxAbsentEmployees,
            long employeeCount) {
    }

    public record DepartmentUpsertRequest(
            @NotBlank @Size(max = 120) String name,
            Long managerId,
            @NotNull @Min(1) Integer maxAbsentEmployees) {
    }

    public record EmployeeDto(
            Long id,
            String name,
            String email,
            Role role,
            Long departmentId,
            String departmentName,
            int annualLeaveDays,
            int availableLeaveDays,
            boolean active) {
    }

    public record EmployeeUpsertRequest(
            @NotBlank @Size(max = 140) String name,
            @NotBlank @Email @Size(max = 180) String email,
            @Size(min = 8, max = 72) String password,
            @NotNull Role role,
            @NotNull Long departmentId,
            @NotNull @Min(0) @Max(365) Integer annualLeaveDays,
            @NotNull @Min(0) @Max(365) Integer availableLeaveDays,
            @NotNull Boolean active) {
    }

    public record LeaveTypeDto(
            Long id,
            String name,
            String code,
            boolean requiresAttachment,
            boolean paid) {
    }

    public record LeaveTypeUpsertRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{2,20}") String code,
            @NotNull Boolean requiresAttachment,
            @NotNull Boolean paid) {
    }

    public record LeaveRequestInput(
            @NotNull Long leaveTypeId,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            @Size(max = 800) String reason) {
    }

    public record DecisionInput(
            @NotNull LeaveStatus decision,
            @Size(max = 1000) String comment) {
    }

    public record AttachmentDto(
            Long id,
            String fileName,
            String contentType,
            long fileSize,
            LocalDateTime uploadedAt) {
    }

    public record WorkflowDto(
            Long id,
            LeaveStatus oldStatus,
            LeaveStatus currentStatus,
            Long changedById,
            String changedByName,
            LocalDateTime changedAt,
            String comment) {
    }

    public record LeaveRequestDto(
            Long id,
            Long employeeId,
            String employeeName,
            Long departmentId,
            String departmentName,
            Long leaveTypeId,
            String leaveTypeName,
            String leaveTypeCode,
            boolean attachmentRequired,
            LocalDate startDate,
            LocalDate endDate,
            int workingDays,
            LeaveStatus status,
            String reason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<AttachmentDto> attachments,
            List<WorkflowDto> workflow) {
    }

    public record LeaveBalanceDto(int available, int consumed, int pending, int annual) {
    }

    public record DashboardDto(
            LeaveBalanceDto balance,
            long totalRequests,
            long pendingRequests,
            long approvedRequests,
            long rejectedRequests,
            long employees,
            long departments,
            List<LeaveRequestDto> recentRequests) {
    }

    public record CalendarEventDto(
            Long requestId,
            String employeeName,
            String departmentName,
            String leaveTypeCode,
            LocalDate startDate,
            LocalDate endDate,
            LeaveStatus status,
            boolean overlapWarning) {
    }

    public record DepartmentStatistic(
            Long departmentId,
            String departmentName,
            long employees,
            long requests,
            long approvedDays,
            long pendingRequests) {
    }

    public record ReportSummaryDto(
            long totalRequests,
            long pendingRequests,
            long approvedRequests,
            long rejectedRequests,
            long cancelledRequests,
            List<DepartmentStatistic> departments) {
    }
}
