package ro.leavehub.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.leavehub.api.ApiDtos.*;
import ro.leavehub.service.AdministrationService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdministrationController {

    private final AdministrationService service;

    @GetMapping("/departments")
    public List<DepartmentDto> departments() {
        return service.departments();
    }

    @PostMapping("/departments")
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentDto createDepartment(@Valid @RequestBody DepartmentUpsertRequest input) {
        return service.createDepartment(input);
    }

    @PutMapping("/departments/{id}")
    public DepartmentDto updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentUpsertRequest input) {
        return service.updateDepartment(id, input);
    }

    @DeleteMapping("/departments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDepartment(@PathVariable Long id) {
        service.deleteDepartment(id);
    }

    @GetMapping("/employees")
    public List<EmployeeDto> employees(@RequestParam(required = false) Long departmentId) {
        return service.employees(departmentId);
    }

    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDto createEmployee(@Valid @RequestBody EmployeeUpsertRequest input) {
        return service.createEmployee(input);
    }

    @PutMapping("/employees/{id}")
    public EmployeeDto updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeUpsertRequest input) {
        return service.updateEmployee(id, input);
    }

    @DeleteMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(@PathVariable Long id) {
        service.deleteEmployee(id);
    }

    @GetMapping("/leave-types")
    public List<LeaveTypeDto> leaveTypes() {
        return service.leaveTypes();
    }

    @PostMapping("/leave-types")
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveTypeDto createLeaveType(@Valid @RequestBody LeaveTypeUpsertRequest input) {
        return service.createLeaveType(input);
    }

    @PutMapping("/leave-types/{id}")
    public LeaveTypeDto updateLeaveType(@PathVariable Long id, @Valid @RequestBody LeaveTypeUpsertRequest input) {
        return service.updateLeaveType(id, input);
    }

    @DeleteMapping("/leave-types/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLeaveType(@PathVariable Long id) {
        service.deleteLeaveType(id);
    }
}
