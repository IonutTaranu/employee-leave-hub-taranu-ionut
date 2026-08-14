package ro.leavehub.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "departments", uniqueConstraints = @UniqueConstraint(name = "uk_department_name", columnNames = "name"))
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", foreignKey = @ForeignKey(name = "fk_department_manager"))
    private Employee manager;

    @Column(name = "max_absent_employees", nullable = false)
    private Integer maxAbsentEmployees;
}
