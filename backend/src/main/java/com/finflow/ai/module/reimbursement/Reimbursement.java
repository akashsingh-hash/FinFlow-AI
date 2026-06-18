package com.finflow.ai.module.reimbursement;

import com.finflow.ai.module.expense.Expense;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reimbursements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reimbursement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReimbursementStatus status;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
