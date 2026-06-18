package com.finflow.ai.module.analytics;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlySpend {
    private String month;
    private BigDecimal amount;
}
