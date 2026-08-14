package ro.leavehub.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "leave_types", uniqueConstraints = {
        @UniqueConstraint(name = "uk_leave_type_name", columnNames = "name"),
        @UniqueConstraint(name = "uk_leave_type_code", columnNames = "code")
})
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(name = "requires_attachment", nullable = false)
    private Boolean requiresAttachment;

    @Column(nullable = false)
    private Boolean paid;
}
