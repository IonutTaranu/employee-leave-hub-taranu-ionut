package ro.leavehub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.leavehub.model.Department;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByNameIgnoreCase(String name);
}
