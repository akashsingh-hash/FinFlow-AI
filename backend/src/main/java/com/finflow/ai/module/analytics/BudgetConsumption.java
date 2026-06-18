package com.finflow.ai.module.analytics;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetConsumption {
    private String department;
    private BigDecimal allocated;
    private BigDecimal utilized;
}
