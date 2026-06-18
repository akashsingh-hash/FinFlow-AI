package com.finflow.ai.module.analytics;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentSpend {
    private String department;
    private BigDecimal amount;
}
