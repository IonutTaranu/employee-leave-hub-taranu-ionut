package ro.leavehub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.leavehub.model.LeaveType;

import java.util.Optional;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    Optional<LeaveType> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
