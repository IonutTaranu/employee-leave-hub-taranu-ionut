package ro.leavehub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.leavehub.model.LeaveRequest;
import ro.leavehub.model.LeaveStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {
    List<LeaveRequest> findAllByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    long countByStatus(LeaveStatus status);

    long countByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    @Query("select coalesce(sum(r.workingDays), 0) from LeaveRequest r where r.employee.id = :employeeId and r.status = :status")
    int sumWorkingDaysByEmployeeAndStatus(@Param("employeeId") Long employeeId, @Param("status") LeaveStatus status);

    @Query("""
            select r from LeaveRequest r
            where r.employee.department.id = :departmentId
              and r.status in :statuses
              and r.startDate <= :toDate
              and r.endDate >= :fromDate
            order by r.startDate, r.employee.name
            """)
    List<LeaveRequest> findOverlappingByDepartment(
            @Param("departmentId") Long departmentId,
            @Param("statuses") Collection<LeaveStatus> statuses,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query("""
            select r from LeaveRequest r
            where r.status in :statuses
              and r.startDate <= :toDate
              and r.endDate >= :fromDate
            order by r.startDate, r.employee.name
            """)
    List<LeaveRequest> findOverlapping(
            @Param("statuses") Collection<LeaveStatus> statuses,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
