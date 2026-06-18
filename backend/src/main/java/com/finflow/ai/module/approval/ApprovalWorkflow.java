package com.finflow.ai.module.approval;

import com.finflow.ai.module.expense.Expense;
import com.finflow.ai.module.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_workflows")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalWorkflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approver_id")
    private User approver; // Nullable when assigned to general FINANCE_MANAGER pool

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status;

    private String comments;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
