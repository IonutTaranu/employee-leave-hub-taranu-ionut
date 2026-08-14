package ro.leavehub.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.leavehub.api.ApiDtos.*;
import ro.leavehub.model.*;
import ro.leavehub.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository requestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveWorkflowRepository workflowRepository;
    private final AttachmentRepository attachmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final CurrentUserService currentUserService;
    private final RomanianHolidayService holidayService;
    private final ApiMapper mapper;
    private final AttachmentService attachmentService;

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> list(
            LeaveStatus status,
            Long departmentId,
            Long leaveTypeId,
            String employee,
            LocalDate from,
            LocalDate to) {
        var current = currentUserService.get();
        Specification<LeaveRequest> specification = (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (current.getRole() == Role.EMPLOYEE) {
                predicates.add(cb.equal(root.get("employee").get("id"), current.getId()));
            } else if (current.getRole() == Role.MANAGER) {
                predicates.add(cb.equal(root.get("employee").get("department").get("id"), current.getDepartment().getId()));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (departmentId != null && current.getRole() == Role.ADMIN) {
                predicates.add(cb.equal(root.get("employee").get("department").get("id"), departmentId));
            }
            if (leaveTypeId != null) {
                predicates.add(cb.equal(root.get("leaveType").get("id"), leaveTypeId));
            }
            if (employee != null && !employee.isBlank() && current.getRole() != Role.EMPLOYEE) {
                predicates.add(cb.like(cb.lower(root.get("employee").get("name")), "%" + employee.trim().toLowerCase() + "%"));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return requestRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream().map(mapper::leaveRequest).toList();
    }

    @Transactional(readOnly = true)
    public LeaveRequestDto get(Long id) {
        var request = find(id);
        assertCanView(request, currentUserService.get());
        return mapper.leaveRequest(request);
    }

    @Transactional
    public LeaveRequestDto create(LeaveRequestInput input) {
        var current = currentUserService.get();
        var type = findType(input.leaveTypeId());
        var workingDays = holidayService.workingDays(input.startDate(), input.endDate());
        var request = LeaveRequest.builder()
                .employee(current)
                .leaveType(type)
                .startDate(input.startDate())
                .endDate(input.endDate())
                .workingDays(workingDays)
                .status(LeaveStatus.DRAFT)
                .reason(clean(input.reason()))
                .build();
        requestRepository.save(request);
        record(request, current, null, LeaveStatus.DRAFT, "Cerere creata");
        return mapper.leaveRequest(request);
    }

    @Transactional
    public LeaveRequestDto update(Long id, LeaveRequestInput input) {
        var current = currentUserService.get();
        var request = find(id);
        assertOwner(request, current);
        if (request.getStatus() != LeaveStatus.DRAFT) {
            throw ApiException.badRequest("Doar cererile in stadiul DRAFT pot fi modificate.");
        }
        request.setLeaveType(findType(input.leaveTypeId()));
        request.setStartDate(input.startDate());
        request.setEndDate(input.endDate());
        request.setWorkingDays(holidayService.workingDays(input.startDate(), input.endDate()));
        request.setReason(clean(input.reason()));
        return mapper.leaveRequest(requestRepository.save(request));
    }

    @Transactional
    public LeaveRequestDto submit(Long id) {
        var current = currentUserService.get();
        var request = find(id);
        assertOwner(request, current);
        if (request.getStatus() != LeaveStatus.DRAFT) {
            throw ApiException.badRequest("Doar o cerere DRAFT poate fi trimisa spre aprobare.");
        }
        if (request.getLeaveType().getRequiresAttachment() && attachmentRepository.countByLeaveRequestId(id) == 0) {
            throw ApiException.badRequest("Tipul de concediu selectat necesita un document justificativ.");
        }
        ensureBalanceForSubmission(request);
        transition(request, current, LeaveStatus.PENDING, "Cerere trimisa spre aprobare");
        return mapper.leaveRequest(request);
    }

    @Transactional
    public LeaveRequestDto cancel(Long id) {
        var current = currentUserService.get();
        var request = find(id);
        assertOwner(request, current);
        if (request.getStatus() != LeaveStatus.DRAFT && request.getStatus() != LeaveStatus.PENDING) {
            throw ApiException.badRequest("Cererea poate fi anulata doar inainte de aprobare.");
        }
        transition(request, current, LeaveStatus.CANCELLED, "Cerere anulata de angajat");
        return mapper.leaveRequest(request);
    }

    @Transactional
    public LeaveRequestDto decide(Long id, DecisionInput input) {
        var current = currentUserService.get();
        var request = find(id);
        if (current.getRole() == Role.EMPLOYEE) {
            throw ApiException.forbidden("Nu aveti dreptul sa aprobati cereri.");
        }
        if (current.getRole() == Role.MANAGER
                && !Objects.equals(current.getDepartment().getId(), request.getEmployee().getDepartment().getId())) {
            throw ApiException.forbidden("Managerul poate procesa doar cererile departamentului propriu.");
        }
        if (Objects.equals(current.getId(), request.getEmployee().getId())) {
            throw ApiException.badRequest("Nu va puteti aproba propria cerere.");
        }
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw ApiException.badRequest("Doar cererile PENDING pot fi aprobate sau respinse.");
        }
        if (input.decision() != LeaveStatus.APPROVED && input.decision() != LeaveStatus.REJECTED) {
            throw ApiException.badRequest("Decizia trebuie sa fie APPROVED sau REJECTED.");
        }
        if (input.decision() == LeaveStatus.REJECTED && (input.comment() == null || input.comment().isBlank())) {
            throw ApiException.badRequest("Comentariul este obligatoriu la respingerea unei cereri.");
        }
        if (input.decision() == LeaveStatus.APPROVED && deductsBalance(request)) {
            var employee = request.getEmployee();
            if (employee.getAvailableLeaveDays() < request.getWorkingDays()) {
                throw ApiException.badRequest("Angajatul nu mai are suficiente zile de concediu disponibile.");
            }
            employee.setAvailableLeaveDays(employee.getAvailableLeaveDays() - request.getWorkingDays());
            employeeRepository.save(employee);
        }
        transition(request, current, input.decision(), clean(input.comment()));
        return mapper.leaveRequest(request);
    }

    @Transactional
    public void delete(Long id) {
        var current = currentUserService.get();
        var request = find(id);
        if (current.getRole() != Role.ADMIN) {
            assertOwner(request, current);
        }
        if (request.getStatus() != LeaveStatus.DRAFT && current.getRole() != Role.ADMIN) {
            throw ApiException.badRequest("Doar cererile DRAFT pot fi sterse.");
        }
        attachmentService.deleteFilesForRequest(id);
        requestRepository.delete(request);
    }

    @Transactional(readOnly = true)
    public DashboardDto dashboard() {
        var current = currentUserService.get();
        var visible = list(null, null, null, null, null, null);
        var pending = visible.stream().filter(r -> r.status() == LeaveStatus.PENDING).count();
        var approved = visible.stream().filter(r -> r.status() == LeaveStatus.APPROVED).count();
        var rejected = visible.stream().filter(r -> r.status() == LeaveStatus.REJECTED).count();
        var consumed = current.getAnnualLeaveDays() - current.getAvailableLeaveDays();
        var pendingDays = requestRepository.sumWorkingDaysByEmployeeAndStatus(current.getId(), LeaveStatus.PENDING);
        var balance = new LeaveBalanceDto(
                current.getAvailableLeaveDays(), consumed, pendingDays, current.getAnnualLeaveDays());
        return new DashboardDto(
                balance, visible.size(), pending, approved, rejected,
                current.getRole() == Role.ADMIN ? employeeRepository.count() : 0,
                current.getRole() == Role.ADMIN ? departmentRepository.count() : 0,
                visible.stream().limit(6).toList());
    }

    @Transactional(readOnly = true)
    public List<CalendarEventDto> calendar(Long departmentId, LocalDate from, LocalDate to) {
        var current = currentUserService.get();
        var effectiveFrom = from == null ? LocalDate.now().withDayOfMonth(1) : from;
        var effectiveTo = to == null ? effectiveFrom.plusMonths(2).minusDays(1) : to;
        Long effectiveDepartment = switch (current.getRole()) {
            case EMPLOYEE, MANAGER -> current.getDepartment().getId();
            case ADMIN -> departmentId;
        };
        var statuses = List.of(LeaveStatus.APPROVED, LeaveStatus.PENDING);
        var requests = effectiveDepartment == null
                ? requestRepository.findOverlapping(statuses, effectiveFrom, effectiveTo)
                : requestRepository.findOverlappingByDepartment(effectiveDepartment, statuses, effectiveFrom, effectiveTo);

        var warningIds = overlapWarnings(requests);
        return requests.stream().map(request -> new CalendarEventDto(
                request.getId(), request.getEmployee().getName(), request.getEmployee().getDepartment().getName(),
                request.getLeaveType().getCode(), request.getStartDate(), request.getEndDate(), request.getStatus(),
                warningIds.contains(request.getId()))).toList();
    }

    public LeaveRequest findAuthorized(Long id) {
        var request = find(id);
        assertCanView(request, currentUserService.get());
        return request;
    }

    private Set<Long> overlapWarnings(List<LeaveRequest> requests) {
        var warningIds = new HashSet<Long>();
        var byDepartment = requests.stream().collect(java.util.stream.Collectors.groupingBy(
                request -> request.getEmployee().getDepartment().getId()));
        byDepartment.values().forEach(departmentRequests -> {
            var limit = departmentRequests.getFirst().getEmployee().getDepartment().getMaxAbsentEmployees();
            var min = departmentRequests.stream().map(LeaveRequest::getStartDate).min(LocalDate::compareTo).orElseThrow();
            var max = departmentRequests.stream().map(LeaveRequest::getEndDate).max(LocalDate::compareTo).orElseThrow();
            for (var day = min; !day.isAfter(max); day = day.plusDays(1)) {
                if (!holidayService.isWorkingDay(day)) {
                    continue;
                }
                var currentDay = day;
                var active = departmentRequests.stream()
                        .filter(r -> !r.getStartDate().isAfter(currentDay) && !r.getEndDate().isBefore(currentDay))
                        .toList();
                var employees = active.stream().map(r -> r.getEmployee().getId()).distinct().count();
                if (employees > limit) {
                    active.forEach(r -> warningIds.add(r.getId()));
                }
            }
        });
        return warningIds;
    }

    private void ensureBalanceForSubmission(LeaveRequest request) {
        if (!deductsBalance(request)) {
            return;
        }
        var alreadyPending = requestRepository.sumWorkingDaysByEmployeeAndStatus(
                request.getEmployee().getId(), LeaveStatus.PENDING);
        if (request.getEmployee().getAvailableLeaveDays() < alreadyPending + request.getWorkingDays()) {
            throw ApiException.badRequest("Soldul disponibil nu acopera toate zilele aflate in asteptare.");
        }
    }

    private boolean deductsBalance(LeaveRequest request) {
        return "CO".equalsIgnoreCase(request.getLeaveType().getCode());
    }

    private void transition(LeaveRequest request, Employee actor, LeaveStatus status, String comment) {
        var oldStatus = request.getStatus();
        request.setStatus(status);
        requestRepository.save(request);
        record(request, actor, oldStatus, status, comment);
    }

    private void record(LeaveRequest request, Employee actor, LeaveStatus oldStatus, LeaveStatus newStatus, String comment) {
        workflowRepository.save(LeaveWorkflow.builder()
                .leaveRequest(request)
                .changedBy(actor)
                .oldStatus(oldStatus)
                .currentStatus(newStatus)
                .changedAt(LocalDateTime.now())
                .comment(comment)
                .build());
    }

    private LeaveRequest find(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Cererea nu exista."));
    }

    private LeaveType findType(Long id) {
        return leaveTypeRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Tipul de concediu nu exista."));
    }

    private void assertOwner(LeaveRequest request, Employee current) {
        if (!Objects.equals(request.getEmployee().getId(), current.getId())) {
            throw ApiException.forbidden("Puteti modifica doar cererile proprii.");
        }
    }

    private void assertCanView(LeaveRequest request, Employee current) {
        if (current.getRole() == Role.ADMIN) {
            return;
        }
        if (current.getRole() == Role.MANAGER
                && Objects.equals(current.getDepartment().getId(), request.getEmployee().getDepartment().getId())) {
            return;
        }
        assertOwner(request, current);
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
