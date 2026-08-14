package ro.leavehub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.leavehub.api.ApiDtos.DepartmentStatistic;
import ro.leavehub.api.ApiDtos.ReportSummaryDto;
import ro.leavehub.model.*;
import ro.leavehub.repository.*;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final LeaveRequestRepository requestRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;
    private final LeaveRequestService leaveRequestService;
    private final PdfReportService pdfReportService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ReportSummaryDto summary() {
        var current = currentUserService.get();
        var requests = visibleRequests(current);
        var departments = current.getRole() == Role.ADMIN
                ? departmentRepository.findAll()
                : List.of(current.getDepartment());
        var statistics = departments.stream().map(department -> {
            var departmentRequests = requests.stream()
                    .filter(request -> request.getEmployee().getDepartment().getId().equals(department.getId())).toList();
            var approvedDays = departmentRequests.stream()
                    .filter(request -> request.getStatus() == LeaveStatus.APPROVED)
                    .mapToLong(LeaveRequest::getWorkingDays).sum();
            var pending = departmentRequests.stream().filter(request -> request.getStatus() == LeaveStatus.PENDING).count();
            return new DepartmentStatistic(
                    department.getId(), department.getName(),
                    employeeRepository.findAllByDepartmentIdOrderByName(department.getId()).size(),
                    departmentRequests.size(), approvedDays, pending);
        }).sorted(Comparator.comparing(DepartmentStatistic::departmentName)).toList();
        return new ReportSummaryDto(
                requests.size(), count(requests, LeaveStatus.PENDING), count(requests, LeaveStatus.APPROVED),
                count(requests, LeaveStatus.REJECTED), count(requests, LeaveStatus.CANCELLED), statistics);
    }

    @Transactional(readOnly = true)
    public byte[] requestPdf(Long requestId) {
        return pdfReportService.leaveRequest(leaveRequestService.findAuthorized(requestId));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public byte[] pendingPdf() {
        var requests = visibleRequests(currentUserService.get()).stream()
                .filter(request -> request.getStatus() == LeaveStatus.PENDING)
                .sorted(Comparator.comparing(LeaveRequest::getCreatedAt)).toList();
        return pdfReportService.pendingRequests(requests);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public byte[] balancesPdf() {
        return pdfReportService.leaveBalances(employeeRepository.findAll().stream()
                .sorted(Comparator.comparing(Employee::getName)).toList());
    }

    private List<LeaveRequest> visibleRequests(Employee current) {
        var requests = requestRepository.findAll();
        if (current.getRole() == Role.MANAGER) {
            return requests.stream()
                    .filter(request -> request.getEmployee().getDepartment().getId().equals(current.getDepartment().getId()))
                    .toList();
        }
        return requests;
    }

    private long count(List<LeaveRequest> requests, LeaveStatus status) {
        return requests.stream().filter(request -> request.getStatus() == status).count();
    }
}
