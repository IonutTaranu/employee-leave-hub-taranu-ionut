package ro.leavehub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.leavehub.model.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<Employee> findAllByDepartmentIdOrderByName(Long departmentId);
}
