package ro.leavehub.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "leave_workflows", indexes = @Index(name = "idx_workflow_request", columnList = "leave_request_id"))
public class LeaveWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_request_id", nullable = false, foreignKey = @ForeignKey(name = "fk_workflow_request"))
    private LeaveRequest leaveRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_id", nullable = false, foreignKey = @ForeignKey(name = "fk_workflow_employee"))
    private Employee changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    private LeaveStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 20)
    private LeaveStatus currentStatus;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(length = 1000)
    private String comment;
}
