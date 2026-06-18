package com.finflow.ai.module.budget.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    private Long id;
    private String department;
    private BigDecimal allocatedAmount;
    private BigDecimal utilizedAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long companyId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
