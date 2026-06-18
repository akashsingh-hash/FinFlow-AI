package com.finflow.ai.module.expense.dto;

import com.finflow.ai.module.expense.ExpenseStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private Long id;
    private String title;
    private BigDecimal amount;
    private String description;
    private ExpenseStatus status;
    private String category;
    private String receiptUrl;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private Long budgetId;
    private String budgetDepartment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
