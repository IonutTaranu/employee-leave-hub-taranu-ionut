package ro.leavehub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.leavehub.api.ApiDtos.*;
import ro.leavehub.model.*;
import ro.leavehub.repository.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdministrationService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;
    private final ApiMapper mapper;

    @Transactional(readOnly = true)
    public List<DepartmentDto> departments() {
        return departmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Department::getName))
                .map(mapper::department).toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public DepartmentDto createDepartment(DepartmentUpsertRequest input) {
        if (departmentRepository.findByNameIgnoreCase(input.name().trim()).isPresent()) {
            throw ApiException.badRequest("Exista deja un departament cu acest nume.");
        }
        var department = Department.builder()
                .name(input.name().trim())
                .maxAbsentEmployees(input.maxAbsentEmployees())
                .build();
        departmentRepository.save(department);
        applyManager(department, input.managerId());
        return mapper.department(departmentRepository.save(department));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public DepartmentDto updateDepartment(Long id, DepartmentUpsertRequest input) {
        var department = findDepartment(id);
        departmentRepository.findByNameIgnoreCase(input.name().trim())
                .filter(existing -> !Objects.equals(existing.getId(), id))
                .ifPresent(existing -> { throw ApiException.badRequest("Exista deja un departament cu acest nume."); });
        department.setName(input.name().trim());
        department.setMaxAbsentEmployees(input.maxAbsentEmployees());
        applyManager(department, input.managerId());
        return mapper.department(departmentRepository.save(department));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDepartment(Long id) {
        var department = findDepartment(id);
        if (!employeeRepository.findAllByDepartmentIdOrderByName(id).isEmpty()) {
            throw ApiException.badRequest("Departamentul nu poate fi sters cat timp are angajati asociati.");
        }
        departmentRepository.delete(department);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public List<EmployeeDto> employees(Long departmentId) {
        var current = currentUserService.get();
        if (current.getRole() == Role.MANAGER) {
            return employeeRepository.findAllByDepartmentIdOrderByName(current.getDepartment().getId())
                    .stream().map(mapper::employee).toList();
        }
        var employees = departmentId == null
                ? employeeRepository.findAll()
                : employeeRepository.findAllByDepartmentIdOrderByName(departmentId);
        return employees.stream().sorted(Comparator.comparing(Employee::getName)).map(mapper::employee).toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeDto createEmployee(EmployeeUpsertRequest input) {
        validateEmployeeInput(input, true);
        if (employeeRepository.existsByEmailIgnoreCase(input.email().trim())) {
            throw ApiException.badRequest("Exista deja un utilizator cu acest email.");
        }
        var employee = Employee.builder()
                .name(input.name().trim())
                .email(input.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(input.password()))
                .role(input.role())
                .department(findDepartment(input.departmentId()))
                .annualLeaveDays(input.annualLeaveDays())
                .availableLeaveDays(input.availableLeaveDays())
                .active(input.active())
                .build();
        return mapper.employee(employeeRepository.save(employee));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeDto updateEmployee(Long id, EmployeeUpsertRequest input) {
        validateEmployeeInput(input, false);
        var employee = findEmployee(id);
        employeeRepository.findByEmailIgnoreCase(input.email().trim())
                .filter(existing -> !Objects.equals(existing.getId(), id))
                .ifPresent(existing -> { throw ApiException.badRequest("Exista deja un utilizator cu acest email."); });
        employee.setName(input.name().trim());
        employee.setEmail(input.email().trim().toLowerCase());
        if (input.password() != null && !input.password().isBlank()) {
            employee.setPasswordHash(passwordEncoder.encode(input.password()));
        }
        employee.setRole(input.role());
        employee.setDepartment(findDepartment(input.departmentId()));
        employee.setAnnualLeaveDays(input.annualLeaveDays());
        employee.setAvailableLeaveDays(input.availableLeaveDays());
        employee.setActive(input.active());
        return mapper.employee(employeeRepository.save(employee));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEmployee(Long id) {
        var current = currentUserService.get();
        if (Objects.equals(current.getId(), id)) {
            throw ApiException.badRequest("Nu va puteti sterge propriul cont.");
        }
        employeeRepository.delete(findEmployee(id));
        employeeRepository.flush();
    }

    @Transactional(readOnly = true)
    public List<LeaveTypeDto> leaveTypes() {
        return leaveTypeRepository.findAll().stream()
                .sorted(Comparator.comparing(LeaveType::getCode))
                .map(mapper::leaveType).toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public LeaveTypeDto createLeaveType(LeaveTypeUpsertRequest input) {
        var code = input.code().trim().toUpperCase();
        if (leaveTypeRepository.existsByCodeIgnoreCase(code)) {
            throw ApiException.badRequest("Exista deja un tip de concediu cu acest cod.");
        }
        var leaveType = LeaveType.builder()
                .name(input.name().trim()).code(code)
                .requiresAttachment(input.requiresAttachment()).paid(input.paid()).build();
        return mapper.leaveType(leaveTypeRepository.save(leaveType));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public LeaveTypeDto updateLeaveType(Long id, LeaveTypeUpsertRequest input) {
        var leaveType = findLeaveType(id);
        var code = input.code().trim().toUpperCase();
        leaveTypeRepository.findByCodeIgnoreCase(code)
                .filter(existing -> !Objects.equals(existing.getId(), id))
                .ifPresent(existing -> { throw ApiException.badRequest("Exista deja un tip de concediu cu acest cod."); });
        leaveType.setName(input.name().trim());
        leaveType.setCode(code);
        leaveType.setRequiresAttachment(input.requiresAttachment());
        leaveType.setPaid(input.paid());
        return mapper.leaveType(leaveTypeRepository.save(leaveType));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteLeaveType(Long id) {
        leaveTypeRepository.delete(findLeaveType(id));
        leaveTypeRepository.flush();
    }

    private void validateEmployeeInput(EmployeeUpsertRequest input, boolean creating) {
        if (input.availableLeaveDays() > input.annualLeaveDays()) {
            throw ApiException.badRequest("Soldul disponibil nu poate depasi soldul anual.");
        }
        if (creating && (input.password() == null || input.password().isBlank())) {
            throw ApiException.badRequest("Parola este obligatorie pentru un utilizator nou.");
        }
    }

    private void applyManager(Department department, Long managerId) {
        if (managerId == null) {
            department.setManager(null);
            return;
        }
        var manager = findEmployee(managerId);
        if (!Objects.equals(manager.getDepartment().getId(), department.getId())) {
            throw ApiException.badRequest("Responsabilul trebuie sa apartina departamentului administrat.");
        }
        if (manager.getRole() != Role.MANAGER && manager.getRole() != Role.ADMIN) {
            throw ApiException.badRequest("Responsabilul trebuie sa aiba rolul MANAGER sau ADMIN.");
        }
        department.setManager(manager);
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Departamentul nu exista."));
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Utilizatorul nu exista."));
    }

    private LeaveType findLeaveType(Long id) {
        return leaveTypeRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Tipul de concediu nu exista."));
    }
}
