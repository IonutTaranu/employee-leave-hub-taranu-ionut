package ro.leavehub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ro.leavehub.api.ApiDtos.*;
import ro.leavehub.model.*;
import ro.leavehub.repository.AttachmentRepository;
import ro.leavehub.repository.EmployeeRepository;
import ro.leavehub.repository.LeaveWorkflowRepository;

@Component
@RequiredArgsConstructor
public class ApiMapper {

    private final EmployeeRepository employeeRepository;
    private final AttachmentRepository attachmentRepository;
    private final LeaveWorkflowRepository workflowRepository;

    public UserSummary user(Employee employee) {
        return new UserSummary(
                employee.getId(), employee.getName(), employee.getEmail(), employee.getRole(),
                employee.getDepartment().getId(), employee.getDepartment().getName(),
                employee.getAnnualLeaveDays(), employee.getAvailableLeaveDays());
    }

    public EmployeeDto employee(Employee employee) {
        return new EmployeeDto(
                employee.getId(), employee.getName(), employee.getEmail(), employee.getRole(),
                employee.getDepartment().getId(), employee.getDepartment().getName(),
                employee.getAnnualLeaveDays(), employee.getAvailableLeaveDays(), employee.getActive());
    }

    public DepartmentDto department(Department department) {
        var manager = department.getManager();
        return new DepartmentDto(
                department.getId(), department.getName(),
                manager == null ? null : manager.getId(), manager == null ? null : manager.getName(),
                department.getMaxAbsentEmployees(),
                employeeRepository.findAllByDepartmentIdOrderByName(department.getId()).size());
    }

    public LeaveTypeDto leaveType(LeaveType leaveType) {
        return new LeaveTypeDto(
                leaveType.getId(), leaveType.getName(), leaveType.getCode(),
                leaveType.getRequiresAttachment(), leaveType.getPaid());
    }

    public AttachmentDto attachment(Attachment attachment) {
        return new AttachmentDto(
                attachment.getId(), attachment.getFileName(), attachment.getContentType(),
                attachment.getFileSize(), attachment.getUploadedAt());
    }

    public WorkflowDto workflow(LeaveWorkflow workflow) {
        return new WorkflowDto(
                workflow.getId(), workflow.getOldStatus(), workflow.getCurrentStatus(),
                workflow.getChangedBy().getId(), workflow.getChangedBy().getName(),
                workflow.getChangedAt(), workflow.getComment());
    }

    public LeaveRequestDto leaveRequest(LeaveRequest request) {
        var attachments = attachmentRepository.findAllByLeaveRequestIdOrderByUploadedAtAsc(request.getId())
                .stream().map(this::attachment).toList();
        var workflow = workflowRepository.findAllByLeaveRequestIdOrderByChangedAtAsc(request.getId())
                .stream().map(this::workflow).toList();
        return new LeaveRequestDto(
                request.getId(), request.getEmployee().getId(), request.getEmployee().getName(),
                request.getEmployee().getDepartment().getId(), request.getEmployee().getDepartment().getName(),
                request.getLeaveType().getId(), request.getLeaveType().getName(), request.getLeaveType().getCode(),
                request.getLeaveType().getRequiresAttachment(), request.getStartDate(), request.getEndDate(),
                request.getWorkingDays(), request.getStatus(), request.getReason(), request.getCreatedAt(), request.getUpdatedAt(),
                attachments, workflow);
    }
}
