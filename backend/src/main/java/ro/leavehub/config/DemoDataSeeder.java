package ro.leavehub.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.leavehub.model.*;
import ro.leavehub.repository.*;
import ro.leavehub.service.RomanianHolidayService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements ApplicationRunner {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveRequestRepository requestRepository;
    private final LeaveWorkflowRepository workflowRepository;
    private final PasswordEncoder passwordEncoder;
    private final RomanianHolidayService holidayService;

    @Value("${app.demo-data:true}")
    private boolean demoData;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!demoData || employeeRepository.count() > 0) {
            return;
        }

        var engineering = departmentRepository.save(Department.builder()
                .name("Engineering").maxAbsentEmployees(2).build());
        var finance = departmentRepository.save(Department.builder()
                .name("Finance").maxAbsentEmployees(1).build());
        var hr = departmentRepository.save(Department.builder()
                .name("Resurse Umane").maxAbsentEmployees(1).build());

        var commonPassword = passwordEncoder.encode("Demo123!");
        var admin = employeeRepository.save(employee(
                "Alexandru Admin", "admin@leavehub.ro", commonPassword, Role.ADMIN, hr));
        var manager = employeeRepository.save(employee(
                "Radu Manager", "manager@leavehub.ro", commonPassword, Role.MANAGER, engineering));
        var financeManager = employeeRepository.save(employee(
                "Ioana Marinescu", "finance.manager@leavehub.ro", commonPassword, Role.MANAGER, finance));
        var ana = employeeRepository.save(employee(
                "Ana Popescu", "ana.popescu@leavehub.ro", commonPassword, Role.EMPLOYEE, engineering));
        var mihai = employeeRepository.save(employee(
                "Mihai Ionescu", "mihai.ionescu@leavehub.ro", commonPassword, Role.EMPLOYEE, engineering));
        var elena = employeeRepository.save(employee(
                "Elena Gheorghe", "elena.gheorghe@leavehub.ro", commonPassword, Role.EMPLOYEE, finance));

        engineering.setManager(manager);
        finance.setManager(financeManager);
        hr.setManager(admin);
        departmentRepository.save(engineering);
        departmentRepository.save(finance);
        departmentRepository.save(hr);

        var co = leaveTypeRepository.save(LeaveType.builder()
                .name("Concediu de odihna").code("CO").requiresAttachment(false).paid(true).build());
        leaveTypeRepository.save(LeaveType.builder()
                .name("Concediu medical").code("CM").requiresAttachment(true).paid(true).build());
        leaveTypeRepository.save(LeaveType.builder()
                .name("Concediu fara plata").code("FP").requiresAttachment(false).paid(false).build());
        var special = leaveTypeRepository.save(LeaveType.builder()
                .name("Eveniment special").code("SPECIAL").requiresAttachment(true).paid(true).build());

        var pastStart = nextWeekday(LocalDate.now().minusMonths(1).withDayOfMonth(8));
        var pastEnd = pastStart.plusDays(4);
        var pastDays = holidayService.workingDays(pastStart, pastEnd);
        ana.setAvailableLeaveDays(ana.getAnnualLeaveDays() - pastDays);
        employeeRepository.save(ana);
        createRequest(ana, co, pastStart, pastEnd, LeaveStatus.APPROVED,
                "Vacanta de vara", manager, "Aprobat. Concediu placut!", 20);

        var pendingStart = nextWeekday(LocalDate.now().plusDays(10));
        createRequest(mihai, co, pendingStart, pendingStart.plusDays(4), LeaveStatus.PENDING,
                "Concediu planificat", mihai, "Cerere trimisa spre aprobare", 3);

        var rejectedStart = nextWeekday(LocalDate.now().plusDays(20));
        createRequest(elena, special, rejectedStart, rejectedStart.plusDays(1), LeaveStatus.REJECTED,
                "Eveniment familial", financeManager, "Documentul justificativ lipseste", 8);

        var draftStart = nextWeekday(LocalDate.now().plusDays(35));
        createRequest(ana, co, draftStart, draftStart.plusDays(2), LeaveStatus.DRAFT,
                "Plan pentru toamna", ana, "Cerere creata", 1);
    }

    private Employee employee(String name, String email, String password, Role role, Department department) {
        return Employee.builder()
                .name(name).email(email).passwordHash(password).role(role).department(department)
                .annualLeaveDays(21).availableLeaveDays(21).active(true).build();
    }

    private void createRequest(
            Employee employee,
            LeaveType type,
            LocalDate start,
            LocalDate end,
            LeaveStatus status,
            String reason,
            Employee actor,
            String comment,
            int daysAgo) {
        var createdAt = LocalDateTime.now().minusDays(daysAgo);
        var request = requestRepository.save(LeaveRequest.builder()
                .employee(employee).leaveType(type).startDate(start).endDate(end)
                .workingDays(holidayService.workingDays(start, end)).status(status).reason(reason)
                .createdAt(createdAt).updatedAt(createdAt).build());
        workflowRepository.save(LeaveWorkflow.builder()
                .leaveRequest(request).changedBy(employee).oldStatus(null).currentStatus(LeaveStatus.DRAFT)
                .changedAt(createdAt).comment("Cerere creata").build());
        if (status != LeaveStatus.DRAFT) {
            workflowRepository.save(LeaveWorkflow.builder()
                    .leaveRequest(request).changedBy(status == LeaveStatus.PENDING ? employee : actor)
                    .oldStatus(LeaveStatus.DRAFT)
                    .currentStatus(status == LeaveStatus.PENDING ? LeaveStatus.PENDING : LeaveStatus.PENDING)
                    .changedAt(createdAt.plusHours(2)).comment("Cerere trimisa spre aprobare").build());
        }
        if (status == LeaveStatus.APPROVED || status == LeaveStatus.REJECTED) {
            workflowRepository.save(LeaveWorkflow.builder()
                    .leaveRequest(request).changedBy(actor).oldStatus(LeaveStatus.PENDING).currentStatus(status)
                    .changedAt(createdAt.plusDays(1)).comment(comment).build());
        }
    }

    private LocalDate nextWeekday(LocalDate date) {
        var result = date;
        while (result.getDayOfWeek() == DayOfWeek.SATURDAY || result.getDayOfWeek() == DayOfWeek.SUNDAY) {
            result = result.plusDays(1);
        }
        return result;
    }
}
