package com.finflow.ai.module.reimbursement.dto;

import com.finflow.ai.module.reimbursement.ReimbursementStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementResponse {
    private Long id;
    private Long expenseId;
    private String expenseTitle;
    private BigDecimal expenseAmount;
    private String expenseCategory;
    private String employeeFullName;
    private ReimbursementStatus status;
    private String paymentMethod;
    private String paymentReference;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
