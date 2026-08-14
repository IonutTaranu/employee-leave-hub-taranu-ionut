package ro.leavehub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.leavehub.model.LeaveWorkflow;

import java.util.List;

public interface LeaveWorkflowRepository extends JpaRepository<LeaveWorkflow, Long> {
    List<LeaveWorkflow> findAllByLeaveRequestIdOrderByChangedAtAsc(Long leaveRequestId);
}
