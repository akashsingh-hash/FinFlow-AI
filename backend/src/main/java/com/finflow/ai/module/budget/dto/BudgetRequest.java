package com.finflow.ai.module.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRequest {
    @NotBlank(message = "Department is required")
    private String department;

    @NotNull(message = "Allocated amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Allocated amount must be greater than zero")
    private BigDecimal allocatedAmount;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}
