package ro.leavehub.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.leavehub.api.ApiDtos.*;
import ro.leavehub.model.LeaveStatus;
import ro.leavehub.service.LeaveRequestService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService service;

    @GetMapping("/dashboard")
    public DashboardDto dashboard() {
        return service.dashboard();
    }

    @GetMapping("/leave-requests")
    public List<LeaveRequestDto> list(
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) String employee,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return service.list(status, departmentId, leaveTypeId, employee, from, to);
    }

    @GetMapping("/leave-requests/{id}")
    public LeaveRequestDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/leave-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequestDto create(@Valid @RequestBody LeaveRequestInput input) {
        return service.create(input);
    }

    @PutMapping("/leave-requests/{id}")
    public LeaveRequestDto update(@PathVariable Long id, @Valid @RequestBody LeaveRequestInput input) {
        return service.update(id, input);
    }

    @PostMapping("/leave-requests/{id}/submit")
    public LeaveRequestDto submit(@PathVariable Long id) {
        return service.submit(id);
    }

    @PostMapping("/leave-requests/{id}/cancel")
    public LeaveRequestDto cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @PostMapping("/leave-requests/{id}/decision")
    public LeaveRequestDto decide(@PathVariable Long id, @Valid @RequestBody DecisionInput input) {
        return service.decide(id, input);
    }

    @DeleteMapping("/leave-requests/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/calendar")
    public List<CalendarEventDto> calendar(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return service.calendar(departmentId, from, to);
    }
}
